package marx.apt.jpa;

import java.io.IOException;
import java.io.OutputStream;

import java.lang.reflect.Field;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Type;
import java.lang.reflect.ParameterizedType;

import java.lang.reflect.WildcardType;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.FilerException;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;

import static javax.lang.model.SourceVersion.RELEASE_6;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;

import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.JavaFileManager;
import javax.tools.StandardLocation;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;

import marx.jpa.persistence.jaxb.Attributes;
import marx.jpa.persistence.jaxb.AssociationOverride;
import marx.jpa.persistence.jaxb.Basic;
import marx.jpa.persistence.jaxb.Column;
import marx.jpa.persistence.jaxb.DiscriminatorColumn;
import marx.jpa.persistence.jaxb.DiscriminatorType;
import marx.jpa.persistence.jaxb.EmptyType;
import marx.jpa.persistence.jaxb.EntityMappings;
import marx.jpa.persistence.jaxb.EnumType;
import marx.jpa.persistence.jaxb.GeneratedValue;
import marx.jpa.persistence.jaxb.GenerationType;
import marx.jpa.persistence.jaxb.Id;
import marx.jpa.persistence.jaxb.Inheritance;
import marx.jpa.persistence.jaxb.InheritanceType;
import marx.jpa.persistence.jaxb.JoinColumn;
import marx.jpa.persistence.jaxb.JoinTable;
import marx.jpa.persistence.jaxb.ManyToMany;
import marx.jpa.persistence.jaxb.ManyToOne;
import marx.jpa.persistence.jaxb.NamedNativeQuery;
import marx.jpa.persistence.jaxb.ObjectFactory;
import marx.jpa.persistence.jaxb.OneToMany;
import marx.jpa.persistence.jaxb.OneToOne;
import marx.jpa.persistence.jaxb.PersistenceUnitMetadata;
import marx.jpa.persistence.jaxb.SequenceGenerator;
import marx.jpa.persistence.jaxb.Transient;

import static marx.apt.jpa.JpaAnnotationsConstants.*;

import static marx.apt.jpa.JpaAnnotationsConstants.PROCESSOR_USER_OPTION_XML_OVERRIDE;
import static marx.apt.jpa.JpaAnnotationsConstants.PROCESSOR_USER_OPTION_UPPER_COLUMN_NAMES;

import static marx.apt.jpa.JpaAnnotationsConstants.JPA_VERSION;

/**
 * Creates orm.xml file based on annotations in JPA-based code.
 */
@SupportedAnnotationTypes("javax.persistence.*")
@SupportedSourceVersion(RELEASE_6)
public class AnnotationEntityProcessor extends AbstractProcessor
{
   private AnnotationEntityProcessorUtils utility = null;
   private ObjectFactory of;        // For use with JAXB-generated classes.
   private EntityMappings em;       // JAXB class for orm.xml root element.
   private Marshaller marshaller;   // JAXB marshalling of Java objects to XML.
   private boolean overrideSourceAnnotations = false;
   private boolean useAllUppercaseColumnNames = false;

   public AnnotationEntityProcessor()
   {
      JAXBContext jc;
      try
      {
         // Prepare JAXB to create XML from Java populated by processing the
         // annotations.
         ClassLoader cl = Class.forName( "marx.jpa.persistence.jaxb.ObjectFactory").getClassLoader();
         jc = JAXBContext.newInstance("marx.jpa.persistence.jaxb", cl);
         this.marshaller = jc.createMarshaller();
         this.of = new ObjectFactory();
         this.em = of.createEntityMappings();
      }
      catch (ClassNotFoundException cnfEx)
      {
         // Not using messager due to exception being thrown in constructor.
         System.err.println("Exception trying to acquire JAXBContext (ClassNotFoundException)"
            + cnfEx.getMessage());
      }
      catch (JAXBException jaxbEx)
      {
         // Not using messager due to exception being thrown in constructor.
         System.err.println("Exception trying to acquire JAXBContext (JAXBException): \n" + 
                            jaxbEx.getMessage());
      }
   }

   @Override
   public void init(final ProcessingEnvironment aProcessingEnv)
   {
      super.init(aProcessingEnv);
      configureFromUserSuppliedOptions();
      utility = new AnnotationEntityProcessorUtils(aProcessingEnv);
   }

   /**
    * Process JPA-specific annotations in Java entity classes.
    * 
    * @param aAnnotations Matching annotations to be processed.
    * @param aRoundEnvironment Annotation processing round environment.
    * @return
    */
   @Override
   public boolean process( final Set<? extends TypeElement> aAnnotations, 
                           final RoundEnvironment aRoundEnvironment )
   {
      processingEnv.getMessager().printMessage(
         Diagnostic.Kind.NOTE,
         "Entered AnnotationEntityProcessor.process ...");

      printProcessorOptions();

      utility.getAllElementsByAnnotations(aAnnotations, aRoundEnvironment);

      Set<? extends Element> elements = aRoundEnvironment.getRootElements();
      
      int elementCounter = 1;

      for (Element element: elements)
      {
         final String simpleName = element.getSimpleName().toString();
         
         processingEnv.getMessager().printMessage(
            Diagnostic.Kind.NOTE,
            "ANOTHER ELEMENT (" + elementCounter++ + "): " + simpleName,
            element);

         handleRootElementAnnotationMirrors(element);
      }

      if (aRoundEnvironment.processingOver())
      {
         processingEnv.getMessager().printMessage(
            Diagnostic.Kind.NOTE,
            "EntityProcessor processing completed." );

         preparePersistenceUnitMetadata();
         prepareFixedEntityMappings();

         writeMappingXmlFile();
      }

      return true;
   }

   /**
    * Prepare contents of EntityMappings XML element that are fixed (not
    * dependent upon anything read in the annotations from the source code).
    */
   private void prepareFixedEntityMappings()
   {
      this.em.setVersion(JPA_VERSION);
   }

   /**
    * Prepare persistence unit metadata.
    */
   private void preparePersistenceUnitMetadata()
   {
      PersistenceUnitMetadata pum = this.of.createPersistenceUnitMetadata();
      if ( this.overrideSourceAnnotations )
      {
         EmptyType overrideSource = this.of.createEmptyType();
         pum.setXmlMappingMetadataComplete(overrideSource);
      }
      this.em.setPersistenceUnitMetadata(pum);
   }

   /**
    * Configure settings from command-line options.
    */
   private void configureFromUserSuppliedOptions()
   {
      Map<String, String> options = processingEnv.getOptions();
      for (Map.Entry option : options.entrySet() )
      {
         final String optionKey = option.getKey().toString();
         if ( optionKey.equals(PROCESSOR_USER_OPTION_XML_OVERRIDE) )
         {
            this.overrideSourceAnnotations =
               Boolean.parseBoolean(option.getValue().toString());
         }
         else if ( optionKey.equals(PROCESSOR_USER_OPTION_UPPER_COLUMN_NAMES) )
         {
            this.useAllUppercaseColumnNames =
               Boolean.parseBoolean(option.getValue().toString());
         }
         else
         {
            processingEnv.getMessager().printMessage(
               Diagnostic.Kind.MANDATORY_WARNING,
               "Unexpected user option [" + optionKey + "] provided.");
         }
      }
   }

   /**
    * Print out options provided to my processing environment.
    * 
    * @return
    */
   private Map<String, String> printProcessorOptions()
   {
      Map<String, String> options = processingEnv.getOptions();
      if ( options.size() > 0 )
      {
         for (Map.Entry option : options.entrySet() )
         {
            processingEnv.getMessager().printMessage(
               Diagnostic.Kind.NOTE,
               "Processor Option " + option.getKey().toString() +
                  " has a value of " + option.getValue().toString() );
         }
      }
      else
      {
         processingEnv.getMessager().printMessage(
            Diagnostic.Kind.NOTE,
            "No processor options were specified.");
      }
      return options;
   }

   /**
    * Process the provided Element for its JPA-related annotations.
    * It is expected that the Element provided will be one of the "root elements"
    * encountered when JPA classes are processed by the annotation processor.
    * This will ensure that annotations such as @Entity and @NamedNativeQuery
    * will be at this level.
    * 
    * @param aElement A "root" element (JPA-decorated class).
    */
   private void handleRootElementAnnotationMirrors(
      final Element aElement)
   {
      processingEnv.getMessager().printMessage(
         Diagnostic.Kind.NOTE,
         "Entered handleElementAnnotationMirrors("
            + aElement.getSimpleName() + ") method..." );
      final String simpleName = aElement.getSimpleName().toString();
      List<? extends AnnotationMirror> annotationMirrors = 
         aElement.getAnnotationMirrors();
      marx.jpa.persistence.jaxb.Entity entity = this.of.createEntity();

      for (AnnotationMirror mirror: annotationMirrors)
      {
         final String annotationType = mirror.getAnnotationType().toString();

         if ( annotationType.equals(javax.persistence.Entity.class.getName()) )
         {
            entity.setClazz(aElement.toString());
            populateEntity(entity, mirror, simpleName);
            furtherPopulateEntity( entity, aElement );
         }
         else if ( annotationType.equals(javax.persistence.NamedNativeQueries.class.getName()) )
         {
            createNativeNamedQuery(mirror);
         }
         else if ( annotationType.equals(javax.persistence.DiscriminatorColumn.class.getName()) )
         {
            addDiscriminatorColumnToEntity(entity, mirror);
         }
         else if ( annotationType.equals(javax.persistence.DiscriminatorValue.class.getName()) )
         {
            addDiscriminatorValueToEntity(entity, mirror);
         }
         else if ( annotationType.equals(javax.persistence.Inheritance.class.getName()) )
         {
            addInheritanceToEntity(entity, mirror);
         }
         else if ( annotationType.equals(javax.persistence.SequenceGenerator.class.getName()) )
         {
            addSequenceGeneratorToEntity(entity, mirror);
         }
         else if (    annotationType.equals(javax.persistence.PersistenceContext.class.getName()) 
                   || annotationType.equals(javax.persistence.PersistenceContexts.class.getName())
                   || annotationType.equals(javax.persistence.PersistenceUnit.class.getName()) 
                   || annotationType.equals(javax.persistence.PersistenceUnits.class.getName()) )
         {
            processingEnv.getMessager().printMessage(
               Diagnostic.Kind.MANDATORY_WARNING,
               "AnnotationType " + annotationType +
                  " should map to the persistence.xml file instead of orm.xml.");
         }
         else    // Flag any JPA annotations available but not handled
         {
            processingEnv.getMessager().printMessage(
               Diagnostic.Kind.MANDATORY_WARNING,
               "AnnotationType " + annotationType + " not handled currently." );
         }
      }  // end of for loop over annotationMirrors

      // An @Entity annotation was encountered and is ready to be added to
      // the JAXB EntityMappings element as a sub-element.
      if ( (entity.getName() != null) && (!entity.getName().isEmpty()) )
      {
         this.em.getEntity().add(entity);
      }
   }

   /**
    * Add Inheritance to JAXB-generated Entity.
    * 
    * @param aEntity Entity to which to add Inheritance.
    * @param aMirror
    */
   private void addInheritanceToEntity(
      final marx.jpa.persistence.jaxb.Entity aEntity,
      final AnnotationMirror aMirror)
   {
      Inheritance inheritance = this.of.createInheritance();
      Map<? extends ExecutableElement, ? extends AnnotationValue> elements =
         processingEnv.getElementUtils().getElementValuesWithDefaults(aMirror);
      for (Map.Entry element : elements.entrySet() )
      {
         final String elementKey = element.getKey().toString();
         if ( elementKey.equals(ANNOTATION_KEY_STRATEGY) )
         {
            final String inheritanceTypeStr = element.getValue().toString();

            // Default setting for InheritanceType is SINGLE_TABLE (specification 9.1.29)
            String basicStrategyTypeStr = javax.persistence.InheritanceType.SINGLE_TABLE.toString();
            if ( inheritanceTypeStr.endsWith(javax.persistence.InheritanceType.SINGLE_TABLE.toString()) )
            {
               basicStrategyTypeStr = javax.persistence.InheritanceType.SINGLE_TABLE.toString();
            }
            else if ( inheritanceTypeStr.endsWith(javax.persistence.InheritanceType.JOINED.toString()) )
            {
               basicStrategyTypeStr = javax.persistence.InheritanceType.JOINED.toString();
            }
            else if ( inheritanceTypeStr.endsWith(javax.persistence.InheritanceType.TABLE_PER_CLASS.toString()) )
            {
               basicStrategyTypeStr = javax.persistence.InheritanceType.TABLE_PER_CLASS.toString();
            }
            else  // not one of the expected values for InheritanceType
            {
               processingEnv.getMessager().printMessage(
                  Diagnostic.Kind.ERROR,
                  "Unexpected InheritanceType (" + inheritanceTypeStr + ") encountered.");
            }

            inheritance.setStrategy( InheritanceType.fromValue(basicStrategyTypeStr) );
         }
      }
      aEntity.setInheritance(inheritance);
   }

   /**
    * Add Sequence Generator to JAXB-generated Entity.
    * 
    * @param aEntity Entity to which to add SequenceGenerator.
    * @param aMirror
    */
   private void addSequenceGeneratorToEntity(
      final marx.jpa.persistence.jaxb.Entity aEntity,
      final AnnotationMirror aMirror )
   {
      SequenceGenerator seqGen = this.of.createSequenceGenerator();
      Map<? extends ExecutableElement, ? extends AnnotationValue> elements =
         processingEnv.getElementUtils().getElementValuesWithDefaults(aMirror);
      for (Map.Entry element : elements.entrySet() )
      {
         final String elementKey = element.getKey().toString();
         if ( elementKey.equals(ANNOTATION_KEY_NAME) )
         {
            seqGen.setName(
               utility.trimDoubleQuotes(element.getValue().toString()) );
         }
         else if ( elementKey.equals(ANNOTATION_KEY_SEQUENCE_NAME) )
         {
            seqGen.setSequenceName(
              utility.trimDoubleQuotes(element.getValue().toString()) );
         }
         else if ( elementKey.equals(ANNOTATION_KEY_ALLOCATION_SIZE ) )
         {
            seqGen.setAllocationSize( Integer.valueOf(element.getValue().toString()) );
         }
         else if ( elementKey.equals(ANNOTATION_KEY_INITIAL_VALUE) )
         {
            seqGen.setInitialValue( Integer.valueOf(element.getValue().toString()) );
         }
      }
      aEntity.setSequenceGenerator(seqGen);
   }

   /**
    * Add DiscriminatorColumn to JAXB-generated Entity.
    * 
    * @param aEntity Entity to which to add DiscriminatorColumn.
    * @param aMirror
    */
   private void addDiscriminatorColumnToEntity(
      final marx.jpa.persistence.jaxb.Entity aEntity,
      final AnnotationMirror aMirror)
   {
      DiscriminatorColumn dc = this.of.createDiscriminatorColumn();
      Map<? extends ExecutableElement, ? extends AnnotationValue> elements =
         processingEnv.getElementUtils().getElementValuesWithDefaults(aMirror);
      for (Map.Entry element : elements.entrySet() )
      {
         if ( element.getKey().toString().equals(ANNOTATION_KEY_NAME) )
         {
            dc.setName(
               utility.trimDoubleQuotes(element.getValue().toString()) );
         }
         else if ( element.getKey().toString().equals(ANNOTATION_KEY_LENGTH) )
         {
            dc.setLength( Integer.parseInt(element.getValue().toString()) );
         }
         else if ( element.getKey().toString().equals(ANNOTATION_KEY_DISCRIMINATOR_TYPE) )
         {
            final String origDiscriminatorTypeStr = element.getValue().toString();

            // Default setting for DiscriminatorType is STRING (specification 9.1.30)
            String basicDiscriminatorTypeStr = javax.persistence.DiscriminatorType.STRING.toString();
            if ( origDiscriminatorTypeStr.endsWith(javax.persistence.DiscriminatorType.CHAR.toString()) )
            {
               basicDiscriminatorTypeStr = javax.persistence.DiscriminatorType.CHAR.toString();
            }
            else if ( origDiscriminatorTypeStr.endsWith(javax.persistence.DiscriminatorType.INTEGER.toString()) )
            {
               basicDiscriminatorTypeStr = javax.persistence.DiscriminatorType.INTEGER.toString();
            }
            else if ( origDiscriminatorTypeStr.endsWith(javax.persistence.DiscriminatorType.STRING.toString()) )
            {
               basicDiscriminatorTypeStr = javax.persistence.DiscriminatorType.STRING.toString();
            }
            else  // not one of the expected values for DiscriminatorType
            {
               processingEnv.getMessager().printMessage(
                  Diagnostic.Kind.ERROR,
                  "Unexpected DiscriminatorType (" + origDiscriminatorTypeStr + ") encountered.");
            }
            dc.setDiscriminatorType( DiscriminatorType.fromValue(basicDiscriminatorTypeStr) );
         }
      }
      aEntity.setDiscriminatorColumn(dc);
   }

   /**
    * Add DiscriminatorValue to Entity.
    * 
    * @param aEntity JAXB-generated Entity to which to add DiscriminatorValue.
    * @param aMirror
    */
   private void addDiscriminatorValueToEntity(
      final marx.jpa.persistence.jaxb.Entity aEntity,
      final AnnotationMirror aMirror)
   {
      Map<? extends ExecutableElement, ? extends AnnotationValue> elements =
         processingEnv.getElementUtils().getElementValuesWithDefaults(aMirror);
      for (Map.Entry element : elements.entrySet() )
      {
         if ( element.getKey().toString().equals(ANNOTATION_KEY_VALUE) )
         {
            aEntity.setDiscriminatorValue(
               utility.trimDoubleQuotes(element.getValue().toString()) );
         }
      }
   }

   /**
    * Write out XML file that is consistent with JPA in-source annotations found
    * in the JPA-based code.
    * 
    * This method only has a few lines of code, but they are all significant.
    * The example here demonstrates writing of an XML file using JAXB
    * marshalling.  It also demonstrates use of the parent class
    * (AbstractProcessor class) and access to that parent class' handle to a
    * ProcessingEnvironment (processingEnv).  Through access to this
    * ProcessingEnvironment, the code gains access to a Filer that it uses
    * to write out the JAXB-generated XML file to the file system.
    */
   private void writeMappingXmlFile()
   {
      processingEnv.getMessager().printMessage(
         Diagnostic.Kind.NOTE,
         "Entered writeMappingXmlFile() method..." );

      try
      {
         JavaFileManager.Location location = StandardLocation.CLASS_OUTPUT;

         /*
            A few interesting observations for Filer.createResource()
            1) Files created with this method are not available for further
               annotation processing.  This is okay in this context because
               we are writing out an XML file and would have no reason to
               attempt to process that XML for Java annotations.
            2) Limitation of using Filer here is that the supported standard
               locations may not really be where we wish to write this XML
               file.  If so, using a different mechanism for writing a file
               might be preferred because the main benefit of Filer is using it
               in conjunction with the annotation processor and we don't need
               that benefit in this case.
            3) The second argument to createResource() is for package name.
               This XML file we are writing is really not a source or class
               file and packaging concepts do not apply.  Therefore, as
               recommended in the Javadoc for this method, we are passing the
               empty string to that argument.
            4) The final argument is the name of the actual file to which the
               XML will be written.
            5) Filer.createResource uses "new" Java support for "..." (ellipses
               and variable arguments - Java 5) [last argument ("Element...")
               may be "elided or null"].
         */
         FileObject fo =
            processingEnv.getFiler().createResource(location, "", "orm.xml");
         OutputStream os = fo.openOutputStream();
      
         this.marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, 
                                     Boolean.TRUE);
         this.marshaller.marshal(this.em, os /* System.err */);
         
         os.close();
      }
      catch (PropertyException propEx)
      {
         processingEnv.getMessager().printMessage(
            Diagnostic.Kind.ERROR,
            "PropertyException trying to write XML mapping file:"
               + propEx.getMessage() );
      }
      catch (JAXBException jaxbEx)
      {
         processingEnv.getMessager().printMessage(
            Diagnostic.Kind.ERROR,
            "JAXBException trying to write XML mapping file:"
               + jaxbEx.getMessage() );
      }
      catch (FilerException filerEx)  // must appear before parent IOException
      {
         processingEnv.getMessager().printMessage(
            Diagnostic.Kind.ERROR,
            "Problem with Processing Environment Filer: "
               + filerEx.getMessage() );
      }
      catch (IOException ioEx)
      {
         processingEnv.getMessager().printMessage(
            Diagnostic.Kind.ERROR,
            "Problem opening file to write ORM XML."
               + ioEx.getMessage() );
      }
   }

   /**
    * Accept an Entity object that corresponds to an Entity XML element and
    * populate the given Entity element with its name as acquired from the
    * in-source @Entity annotation or use entity class' name as default name if
    * it is not explicitly specified in the annotation.
    * 
    * @param aEntity XML Entity element for which entity name should be supplied.
    * @param aMirror In-code Entity's annotation mirror.
    * @param aSimpleName Name of class holding @Entity annotation.
    */
   private void populateEntity( final marx.jpa.persistence.jaxb.Entity aEntity,
                                final AnnotationMirror aMirror,
                                final String aSimpleName )
   {
      processingEnv.getMessager().printMessage(
         Diagnostic.Kind.NOTE,
         "Entered createEntity(Entity,AnnotationMirror,String)..." );

      try
      {
         Map<? extends ExecutableElement, ? extends AnnotationValue> mirrorMap = 
            aMirror.getElementValues();

         String entityName = aSimpleName; // Use element's name by default

         for (Map.Entry mirrorEntry : mirrorMap.entrySet())
         {
            String mirrorKey = mirrorEntry.getKey().toString();

            // The name() attribute of the Entity annotation will only be
            // available if it was explicitly set when that annotation was used.
            // If it was not explicitly set, this attribute will not be available.
            if ( mirrorKey.equals(ANNOTATION_KEY_NAME) )
            {
               // The string-formatted entity name includes double quotes
               // that must be removed before storing names in XML.
               entityName =
                  utility.trimDoubleQuotes(mirrorEntry.getValue().toString());
            }
         }

         aEntity.setName(entityName);
      }
      catch (Exception ex)
      {
         processingEnv.getMessager().printMessage(
            Diagnostic.Kind.ERROR,
            "createEntity: " + ex.getMessage() );
      }
   }

   /**
    * Populate existing Entity with additional information based on annotations
    * on the Entity Class' enclosed elements.
    * 
    * @param aEntity Entity class for persisting information to orm.xml file
    *                in <Entity> tag.
    * @param aElement Element representing entity class.
    */
   private void furtherPopulateEntity(
      final marx.jpa.persistence.jaxb.Entity aEntity,
      final Element aElement)
   {
      List<? extends Element> enclosedElements = aElement.getEnclosedElements();
      Attributes entityAttributes = this.of.createAttributes();
      Id entityId = this.of.createId();  // Should have one Id per entity.
      
      for ( Element subElement : enclosedElements )
      {
         ManyToMany currentManyToMany = null;
         ManyToOne currentManyToOne = null;
         OneToMany currentOneToMany = null;
         OneToOne currentOneToOne = null;
         
         Basic basic = null;
         Column column = null;
         JoinColumn jc = null;

         List<? extends AnnotationMirror> entityAnnotations =
            processingEnv.getElementUtils().getAllAnnotationMirrors(subElement);

         for ( AnnotationMirror annotation : entityAnnotations )
         {
            final String annotationType = annotation.getAnnotationType().toString();

            if ( annotationType.equals(javax.persistence.Id.class.getName()) )
            {
               entityId.setColumn(column);
               entityId.setName(subElement.toString());
            }
            else if ( annotationType.equals(javax.persistence.GeneratedValue.class.getName()) )
            {
               addGeneratedValueToEntityId(entityId, annotation);
            }
            else if ( annotationType.equals(javax.persistence.Enumerated.class.getName()) )
            {
               basic = getBasicColumnWithEnumerated(annotation);               
            }
            else if ( annotationType.equals(javax.persistence.Transient.class.getName()) )
            {
               // @Transient annotation does not have any attributes.
               Transient transientAnnotation = this.of.createTransient();
               transientAnnotation.setName(subElement.toString());
               entityAttributes.getTransient().add(transientAnnotation);
            }
            else if ( annotationType.equals(javax.persistence.JoinColumn.class.getName()) )
            {
               jc = this.of.createJoinColumn();
               jc.setName(getJoinColumnName(annotation));
               AssociationOverride assocOverride = this.of.createAssociationOverride();
               assocOverride.getJoinColumn().add(jc);
               assocOverride.setName(subElement.toString());
               aEntity.getAssociationOverride().add(assocOverride);
            }
            else if ( annotationType.equals(javax.persistence.ManyToMany.class.getName()) )
            {
               currentManyToMany =
                  addManyToManyToAttributes( subElement.getSimpleName().toString(),
                                             utility.getGenericParameterType(subElement),
                                             annotation,
                                             entityAttributes);
            }
            else if ( annotationType.equals(javax.persistence.JoinTable.class.getName()) )
            {
               JoinTable jt = getJoinTable(annotation);
               addJoinTableToAppropriateMapping( jt,
                                                 currentManyToMany,
                                                 currentManyToOne,
                                                 currentOneToMany,
                                                 currentOneToOne );
            }
            else if ( annotationType.equals(javax.persistence.Column.class.getName()) )
            {
               if ( column == null )
               {
                  column = this.of.createColumn();
               }
               handleColumnAnnotation(column, annotation);
               basic = this.of.createBasic();
               basic.setColumn(column);
            }
            else
            {
               processingEnv.getMessager().printMessage(
                  Diagnostic.Kind.ERROR,
                  "****** ATTENTION: Need to handle AnnotationType: " +
                     annotationType + " ******" );
            }
         }
         if ( basic != null )
         {
            basic.setName(subElement.toString());
            entityAttributes.getBasic().add(basic);
         }
      }
      if ( (entityId.getName() != null) && (!entityId.getName().isEmpty()) )
      {
         entityAttributes.getId().add(entityId);
         aEntity.setAttributes(entityAttributes);
      }
   }

   /**
    * Parse @Column notation and store its information (atttributes, etc.)
    * in a JAXB Column for later writing to mapping file.
    *
    * @param aColumn The JAXB Column to write out Column element.
    * @param aMirror Mirror providing access to contents of @Column
    *                annotation.
    */
   public void handleColumnAnnotation( final Column aColumn,
                                       final AnnotationMirror aMirror )
   {
      Map<? extends ExecutableElement, ? extends AnnotationValue> mirrorMap = 
         aMirror.getElementValues();
      
      for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> mirrorEntry : mirrorMap.entrySet())
      {
         final String mirrorKey = mirrorEntry.getKey().toString();
         
         if ( mirrorKey.equals(ANNOTATION_KEY_NAME) )
         {
            aColumn.setName( utility.trimDoubleQuotes(mirrorEntry.getValue().toString()) );
         }
         else if ( mirrorKey.equals(ANNOTATION_KEY_LENGTH) )
         {
            aColumn.setLength( Integer.parseInt(mirrorEntry.getValue().toString()) );
         }
         else if ( mirrorKey.equals(ANNOTATION_KEY_COLUMN_DEFINITION) )
         {
            aColumn.setColumnDefinition( utility.trimDoubleQuotes(mirrorEntry.getValue().toString()) );
         }
         else if ( mirrorKey.equals(ANNOTATION_KEY_INSERTABLE) )
         {
            aColumn.setInsertable( Boolean.parseBoolean(mirrorEntry.getValue().toString()) );
         }
         else if ( mirrorKey.equals(ANNOTATION_KEY_NULLABLE) )
         {
            aColumn.setNullable( Boolean.parseBoolean(mirrorEntry.getValue().toString()) );
         }
         else
         {
            // TBD/TODO - There are additional attributes of the @Column
            //            annotation not handled yet above that will need
            //            to be handled as encountered (such as precision,
            //            scale, table, unique, and updatable).
            processingEnv.getMessager().printMessage(
               Diagnostic.Kind.MANDATORY_WARNING,
               "Found unexpected value [" + mirrorKey +
                  "] in handleColumnAnnotation" );
         }
      }
   }

   /**
    * Add JoinTable to appropriate mapping.
    * 
    * @param aJoinTable
    * @param aManyToMany
    * @param aManyToOne
    * @param aOneToMany
    * @param aOneToOne
    */
   private void addJoinTableToAppropriateMapping(
      final JoinTable aJoinTable,
      final ManyToMany aManyToMany,
      final ManyToOne aManyToOne,
      final OneToMany aOneToMany,
      final OneToOne aOneToOne )
   {  
      String noteAboutJoinTable = null;
      Diagnostic.Kind messageKind = Diagnostic.Kind.NOTE;
      
      if ( aManyToMany != null )
      {
         aManyToMany.setJoinTable(aJoinTable);
         noteAboutJoinTable = "An M:N relationship has had JoinTable added.";
      }
      else if ( aManyToOne != null )
      {
         aManyToOne.setJoinTable(aJoinTable);
         noteAboutJoinTable = "An M:1 relationship has had JoinTable added.";
      }
      else if ( aOneToMany != null )
      {
         aOneToMany.setJoinTable(aJoinTable);
         noteAboutJoinTable = "A 1:M relationship has had JoinTable added.";
      }
      else if ( aOneToOne != null)
      {
         aOneToOne.setJoinTable(aJoinTable);
         noteAboutJoinTable = "A 1:1 relationship has had JoinTable added.";
      }
      else
      {
         noteAboutJoinTable = "No valid relationship found to add JoinTable.";
         messageKind = Diagnostic.Kind.MANDATORY_WARNING;
      }
      processingEnv.getMessager().printMessage(messageKind, noteAboutJoinTable);
   }

   /**
    * Acquire JoinTable, if present, from provided AnnotationMirror.
    * 
    * @param aMirror AnnotationMirror with Join Table information.
    * @return JoinTable created from annotation.
    */
   private JoinTable getJoinTable( final AnnotationMirror aMirror )
   {
      JoinTable jt = this.of.createJoinTable();
      Map<? extends ExecutableElement, ? extends AnnotationValue> mirrorMap = 
         aMirror.getElementValues();
      
      for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> mirrorEntry : mirrorMap.entrySet())
      {
         final String mirrorKey = mirrorEntry.getKey().toString();
         
         if ( mirrorKey.equals(ANNOTATION_KEY_NAME) )
         {
            jt.setName( utility.trimDoubleQuotes(mirrorEntry.getValue().toString()) );
         }
         else if ( mirrorKey.equals(ANNOTATION_KEY_JOIN_COLUMNS) )
         {
            List <? extends AnnotationValue> joinColumnAnns =
               extractIndividualJoinColumns(mirrorEntry.getValue());
            for (AnnotationValue annVal : joinColumnAnns)
            {
               jt.getJoinColumn().add( buildJoinColumnObject(annVal) );
            }
         }
         else if ( mirrorKey.equals(ANNOTATION_KEY_INVERSE_JOIN_COLUMNS) )
         {
            List <? extends AnnotationValue> invJoinColumnAnns =
               extractIndividualJoinColumns(mirrorEntry.getValue());
            for (AnnotationValue annVal : invJoinColumnAnns)
            {
               jt.getInverseJoinColumn().add( buildJoinColumnObject(annVal) );
            }
         }
         else
         {
             processingEnv.getMessager().printMessage(
                Diagnostic.Kind.MANDATORY_WARNING,
                "Found unexpected value [" + mirrorKey + "] in getJoinTable" );
         }
      }
      
      return jt;
   }

   /**
    * Add ManyToMany to Attributes.
    *
    * @param aName Name of mapping (same as element's name).
    * @param aMirror AnnotationMirror representing @ManyToMany annotation.
    * @param aAttributes Attributes to which to add ManyToMany.
    * @return
    */
   private ManyToMany addManyToManyToAttributes( final String aName,
                                                 final String aParameterType,
                                                 final AnnotationMirror aMirror,
                                                 final Attributes aAttributes )
   {
      ManyToMany mtom = this.of.createManyToMany();
      mtom.setName(aName);
      aAttributes.getManyToMany().add(mtom);
      
      // TODO -- @ManyToMany annotation has multiple attributes.
      //         Code below should be expanded to handle these attributes.
      Map<? extends ExecutableElement, ? extends AnnotationValue> mirrorMap = 
         aMirror.getElementValues();

      for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> mirrorEntry : mirrorMap.entrySet())
      {
         final String mirrorKey = mirrorEntry.getKey().toString();
      
         if ( mirrorKey.equals(ANNOTATION_KEY_MAPPED_BY) )
         {
            mtom.setMappedBy(
               utility.trimDoubleQuotes(mirrorEntry.getValue().toString()) );
         }
      }
      // Specify target entity associated with this entity via this M:M.
      mtom.setTargetEntity(aParameterType);
      return mtom;
   }

   /**
    * Retrieve JoinColumn name from provided annotation mirror.
    * 
    * @param aMirror AnnotationMirror with @JoinColumn information.
    * @return Name of JoinColumn.
    */
   private String getJoinColumnName(
      final AnnotationMirror aMirror )
   {
      Map<? extends ExecutableElement, ? extends AnnotationValue> mirrorMap = 
         aMirror.getElementValues();

      String jcName = null;
      for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> mirrorEntry : mirrorMap.entrySet())
      {
         final String mirrorKey = mirrorEntry.getKey().toString();

         if ( mirrorKey.equals(ANNOTATION_KEY_NAME) )
         {
            jcName = utility.trimDoubleQuotes(mirrorEntry.getValue().toString());
         }
      }
      return jcName;
   }

   /**
    * Add JoinColumn to AssociationOverride.
    * 
    * @param aMirror
    * @param aAssocOverride
    */
   private void addJoinColumnToAssocOverride(
      final AnnotationMirror aMirror,
      final AssociationOverride aAssocOverride )
   {
      Map<? extends ExecutableElement, ? extends AnnotationValue> mirrorMap = 
         aMirror.getElementValues();
      
      for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> mirrorEntry : mirrorMap.entrySet())
      {
         final String mirrorKey = mirrorEntry.getKey().toString();

         if ( mirrorKey.equals(ANNOTATION_KEY_NAME) )
         {
            JoinColumn jc = this.of.createJoinColumn();
            jc.setName(
               utility.trimDoubleQuotes(mirrorEntry.getValue().toString()) );
            aAssocOverride.getJoinColumn().add(jc);
         }
      }
   }

   /**
    * Provide a BasicColumn with enumerated sub-element based on provided
    * annotation mirror.  Provided annotation mirror should correspond to an
    * @Enumerated annotation.
    * 
    * @param aMirror Annotation Mirror for an @Enumerated annotation.
    * @return 
    */
   private Basic getBasicColumnWithEnumerated(
      final AnnotationMirror aMirror )
   {
      Basic basic = this.of.createBasic();

      Map <? extends ExecutableElement, ? extends AnnotationValue> mirrorMap =
         aMirror.getElementValues();

      for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> mirrorEntry : mirrorMap.entrySet())
      {
         final String mirrorKey = mirrorEntry.getKey().toString();

         if ( mirrorKey.equals(ANNOTATION_KEY_VALUE))
         {
            final String enumeratedValue = mirrorEntry.getValue().toString();
            
            // Default setting for EnumType is ORDINAL (specification 9.1.21)
            String enumString = javax.persistence.EnumType.ORDINAL.toString();
            if ( enumeratedValue.endsWith(javax.persistence.EnumType.ORDINAL.toString()) )
            {
               enumString = javax.persistence.EnumType.ORDINAL.toString();
            }
            else if ( enumeratedValue.endsWith(javax.persistence.EnumType.STRING.toString()) )
            {
               enumString = javax.persistence.EnumType.STRING.toString();
            }
            else  // not one of the expected values for EnumType
            {
               processingEnv.getMessager().printMessage(
                  Diagnostic.Kind.ERROR,
                  "Unexpected EnumType (" + enumeratedValue + ") encountered.");
            }
            basic.setEnumerated(EnumType.fromValue(enumString));
         }         
      }
      return basic;
   }

   /**
    * Add annotated GeneratedValue to EntityId.
    * 
    * @param aEntityId EntityId to which to add GeneratedValue.
    * @param aMirror
    */
   private void addGeneratedValueToEntityId( final Id aEntityId,
                                             final AnnotationMirror aMirror )
   {
      GeneratedValue gv = this.of.createGeneratedValue();
   
      Map<? extends ExecutableElement, ? extends AnnotationValue> mirrorMap = 
         aMirror.getElementValues();
      
      for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> mirrorEntry : mirrorMap.entrySet())
      {
         final String mirrorKey = mirrorEntry.getKey().toString();

         if ( mirrorKey.equals(ANNOTATION_KEY_STRATEGY) )
         {
            final String strategyValue = mirrorEntry.getValue().toString();

            // Default setting for GenerationType is AUTO (specification 9.1.9)
            String basicString = javax.persistence.GenerationType.AUTO.toString();
            if ( strategyValue.endsWith(javax.persistence.GenerationType.SEQUENCE.toString()) )
            {
               basicString = javax.persistence.GenerationType.SEQUENCE.toString();
            }
            else if ( strategyValue.endsWith(javax.persistence.GenerationType.IDENTITY.toString()) )
            {
               basicString = javax.persistence.GenerationType.IDENTITY.toString();
            }
            else if ( strategyValue.endsWith(javax.persistence.GenerationType.TABLE.toString()) )
            {
               basicString = javax.persistence.GenerationType.TABLE.toString();
            }
            else if ( strategyValue.endsWith(javax.persistence.GenerationType.AUTO.toString()) )
            {
               basicString = javax.persistence.GenerationType.AUTO.toString();
            }
            else  // not one of the expected values for GenerationType
            {
               processingEnv.getMessager().printMessage(
                  Diagnostic.Kind.ERROR,
                  "Unexpected GenerationType (" + strategyValue + ") encountered.");
            }
            
            gv.setStrategy( GenerationType.fromValue(basicString) );
         }
         else if ( mirrorKey.equals(ANNOTATION_KEY_GENERATOR) )
         {
            gv.setGenerator(
               utility.trimDoubleQuotes(mirrorEntry.getValue().toString()));
         }
      }
      aEntityId.setGeneratedValue(gv);
   }

   /**
    * Create NativeNamedQuery from in-source annotation. 
    *
    * @param aMirror
    */
   private void createNativeNamedQuery(final AnnotationMirror aMirror)
   {
      processingEnv.getMessager().printMessage(
         Diagnostic.Kind.NOTE,
         "Entered createNativeNamedQuery(AnnotationMirror) method ...");

      try
      {
         Map<? extends ExecutableElement, ? extends AnnotationValue> mirrorMap = 
            aMirror.getElementValues();
         
         for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> mirrorEntry : mirrorMap.entrySet())
         {
            final String mirrorKey = mirrorEntry.getKey().toString();

            // The value() attribute of the NamedNativeQueries annotation
            // contains individual NamedNativeQuery annotations.
            if ( mirrorKey.equals(ANNOTATION_KEY_VALUE) )
            {
               List <? extends AnnotationValue> anns =
                  extractIndividualNativeNamedQueries(mirrorEntry.getValue());
               for (AnnotationValue annVal : anns)
               {
                  this.em.getNamedNativeQuery().add(
                     buildNativeNamedQueryObject(annVal) );
               }
            }
         }
      }
      catch (Exception ex)
      {
         processingEnv.getMessager().printMessage(
            Diagnostic.Kind.ERROR,
            "createEntity: " + ex.getMessage() );
      }
   }

   /**
    * Acquire the AnnotationMirror associated with a NamedNativeQuery annotation
    * that is in AnnotationValue format (most likely because it was obtained as
    * the AnnotationValue for the NamedNativeQueries annotation).
    * 
    * @param aAnnotationValue AnnotationValue form of an NamedNativeQuery annotation.
    * @return Annotation Mirror providing access to NamedNativeQuery attributes.
    */
   private AnnotationMirror acquireNativeNamedQueryAnnotationMirror(
      final AnnotationValue aAnnotationValue )
   {
      return (AnnotationMirror) aAnnotationValue;
   }

   /**
    * Instantiate and populate a JAXB-generated NamedNativeQuery object with
    * appropriate values from the provided NamedNativeQuery annotation.
    *
    * @param aAnnotationValue NamedNativeQuery (most likely obtained in this
    *                         form from a NamedNativeQueries annotation).
    * @return Newly instantiated and populated NamedNativeQuery object.
    */
   private NamedNativeQuery buildNativeNamedQueryObject(
      final AnnotationValue aAnnotationValue)
   {
      NamedNativeQuery nnq = this.of.createNamedNativeQuery();
      AnnotationMirror am = acquireNativeNamedQueryAnnotationMirror(aAnnotationValue);
      Map<? extends ExecutableElement,? extends AnnotationValue> amMap =
         am.getElementValues();
      for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> amMirrorEntry : amMap.entrySet())
      {
         final String amMirrorKey = amMirrorEntry.getKey().toString();
         if ( amMirrorKey.equals(ANNOTATION_KEY_NAME) )
         {
            nnq.setName(
               utility.trimDoubleQuotes(amMirrorEntry.getValue().toString()));
         }
         else if ( amMirrorKey.equals(ANNOTATION_KEY_QUERY) )
         {
            nnq.setQuery(
               utility.trimDoubleQuotes(amMirrorEntry.getValue().toString()));
         }
      }
      return nnq;
   }

   /**
    * Build up a JAXB-compliant JoinColumn object for writing a JoinColumn XML
    * entry.
    * 
    * @param aAnnotationValue
    * @return
    */
   private JoinColumn buildJoinColumnObject(
      final AnnotationValue aAnnotationValue)
   {
      JoinColumn jc = this.of.createJoinColumn();
      AnnotationMirror am = (AnnotationMirror) aAnnotationValue;
      Map<? extends ExecutableElement,? extends AnnotationValue> amMap =
         am.getElementValues();
      for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> amMirrorEntry : amMap.entrySet())
      {
         final String amMirrorKey = amMirrorEntry.getKey().toString();
         if ( amMirrorKey.equals(ANNOTATION_KEY_NAME) )
         {
            jc.setName(
               utility.trimDoubleQuotes(amMirrorEntry.getValue().toString()));
         }
         else if ( amMirrorKey.equals(ANNOTATION_KEY_REFERENCED_COLUMN_NAME) )
         {
            jc.setReferencedColumnName(
               utility.trimDoubleQuotes(amMirrorEntry.getValue().toString()));
         }
         else
         {
            processingEnv.getMessager().printMessage(
               Diagnostic.Kind.MANDATORY_WARNING,
               "Unexpected annotation key [" + amMirrorKey + "] found in buildJoinColumnObject" );
         }
      }
      return jc;
   }

   /**
    * Extract individual native named queries from AnnotationValue for an
    * encompassing NativeNamedQueries annotation.
    * 
    * @param aAnnotationValue NativeNamedQueries' AnnotationValue.
    * @return List of individual NativeNamedQuery annotations.
    */
   private List<? extends AnnotationValue> extractIndividualNativeNamedQueries(
      AnnotationValue aAnnotationValue)
   {
      return (List<? extends AnnotationValue>) aAnnotationValue.getValue();
   }
   
   private List<? extends AnnotationValue> extractIndividualJoinColumns(
      AnnotationValue aAnnotationValue)
   {
      return (List<? extends AnnotationValue>) aAnnotationValue.getValue();
   }
}
