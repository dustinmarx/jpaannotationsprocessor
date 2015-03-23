package marx.apt.jpa;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;

import javax.annotation.processing.RoundEnvironment;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

import javax.tools.Diagnostic;

/**
 * Utility functions for helping with annotation entity processing.
 */
public class AnnotationEntityProcessorUtils
{
   private ProcessingEnvironment processingEnvironment = null;

   /**
    * Default constructor intentionally made private so that it will not be
    * instantiated.
    */
   private AnnotationEntityProcessorUtils()
   {
   }

   /**
    * Constructor accepting a ProcessingEnvironment.
    */
   public AnnotationEntityProcessorUtils(ProcessingEnvironment aProcessingEnvironment)
   {
      this.processingEnvironment = aProcessingEnvironment;
   }

   /**
    * Provide all elements associated with discovered annotations.
    * 
    * @param aAnnotations The annotations of interest.
    * @param aRoundEnvironment Environment for annotation processing.
    */
   public void getAllElementsByAnnotations( Set<? extends TypeElement> aAnnotations,
                                            RoundEnvironment aRoundEnvironment )
   {
      for ( TypeElement element2 : aAnnotations )
      {
         Set<? extends Element> subElements = aRoundEnvironment.getElementsAnnotatedWith(element2);
         
         processingEnvironment.getMessager().printMessage(
            Diagnostic.Kind.NOTE,
            subElements.size() + " Element(s) associated with Annotation @" + element2.getSimpleName() );
            
         for ( Element subElement : subElements )
         {
            processingEnvironment.getMessager().printMessage(
               Diagnostic.Kind.NOTE,
               "[" + subElement.getEnclosingElement().getSimpleName() + "]",
               subElement );
         }
      }
   }

   /**
    * Displays and returns all root elements associated with an annotation
    * processing environment.
    * 
    * @param aRoundEnvironment Round Environment for current processing round.
    * @return Root elements.
    */
   public Set<? extends Element> getAndShowAllRootElements(
      RoundEnvironment aRoundEnvironment)
   {
      Set<? extends Element> elements = 
         aRoundEnvironment.getRootElements();
      processingEnvironment.getMessager().printMessage(
         Diagnostic.Kind.NOTE,
         "Found " + elements.size() + " 'Root' Elements!" );
      int elementCounter = 1;
      for (Element element : elements)
      {
         processingEnvironment.getMessager().printMessage(
            Diagnostic.Kind.NOTE,
            elementCounter + ". " + element.getSimpleName(),
            element );
            
         elementCounter++;
      }
      
      return elements;
   }

   /**
    * Print basic information for the provided Element.
    * 
    * @param aElement Element whose basic information is to be printed.
    */
   public void printBasicElementInformation(Element aElement)
   {
      processingEnvironment.getMessager().printMessage(
         Diagnostic.Kind.NOTE,
         "Simple Name:       " + aElement.getSimpleName() );
      processingEnvironment.getMessager().printMessage(
         Diagnostic.Kind.NOTE,
         "Element Kind:      " + aElement.getKind().toString() );
      processingEnvironment.getMessager().printMessage(
         Diagnostic.Kind.NOTE,
         "Class:             " + aElement.getClass() );
      processingEnvironment.getMessager().printMessage(
         Diagnostic.Kind.NOTE,
         "Enclosing Element: " + aElement.getEnclosingElement().getSimpleName() );
      processingEnvironment.getMessager().printMessage(
         Diagnostic.Kind.NOTE,
         "Type:              " + aElement.asType() );      
   }

   /**
    * Print out elements enclosed by the supplied element.
    * 
    * @param aElement Element whose sub-elements are desired.
    */
   public void printElementEnclosedElements(Element aElement)
   {
      processingEnvironment.getMessager().printMessage(
         Diagnostic.Kind.NOTE,
         "Entered printElementEnclosedElements(Element) method." );
      List<? extends Element> enclosedElements = aElement.getEnclosedElements();
      for (Element enclosedElement : enclosedElements)
      {
         processingEnvironment.getMessager().printMessage(
            Diagnostic.Kind.NOTE,
            "Name: " + enclosedElement.getSimpleName()
                     + " (" + enclosedElement.asType() + ")" );
      }
   }

   /**
    * Print out the modifiers on the specified Element.
    * 
    * @param aElement Element whose modifiers are desired.
    */
   public void printElementModifiers(Element aElement)
   {
      processingEnvironment.getMessager().printMessage(
         Diagnostic.Kind.NOTE,
         "Entered printElementModifiers(Element) method." );
      List<? extends Element> enclosedElements = aElement.getEnclosedElements();
      for (Element enclosedElement : enclosedElements)
      {
         processingEnvironment.getMessager().printMessage(
            Diagnostic.Kind.NOTE,
            "Name: " + enclosedElement.getSimpleName()
                     + " (" + enclosedElement.asType() + ")" );
      }
   }

   /**
    * Print ExecutableElement values based on Elements.getElementValuesWithDefaults
    * and use of the passed-in AnnotationMirror.
    * 
    * @param aMirror AnnotationMirror to be used in conjunction with the
    *                Elements.getElementValuesWithDefaults method.
    */
   public void printElementValuesByMirror(AnnotationMirror aMirror)
   {
      processingEnvironment.getMessager().printMessage(
         Diagnostic.Kind.NOTE,
         "Entered printElementValuesByMirror(AnnotationMirror) method." );
      Elements elementsUtil = processingEnvironment.getElementUtils();
      Map<? extends ExecutableElement, ? extends AnnotationValue> elements =
         elementsUtil.getElementValuesWithDefaults(aMirror);
      for (Map.Entry element : elements.entrySet() )
      {
         processingEnvironment.getMessager().printMessage(
            Diagnostic.Kind.NOTE,
            "Mirror Key: " + element.getKey() + 
               ";    Mirror Value: " + element.getValue() );
      }
   }

   /**
    * Show all members of each of the provided TypeElements.
    * 
    * @param aTypeElements TypeElements from which the member elements are desired.
    */
   public void haveFunWithElementUtils(Set<? extends TypeElement> aTypeElements)
   {
      for ( TypeElement typeElement : aTypeElements )
      {
         haveFunWithElementUtils(typeElement);
      }
   }

   /**
    * Show all elements using Elements.getAllMembers.
    * 
    * @param aTypeElement TypeElement from which to get all members.
    */
   public void haveFunWithElementUtils(TypeElement aTypeElement)
   {
      Elements elements = processingEnvironment.getElementUtils();
      
      List <? extends Element> allElements = elements.getAllMembers(aTypeElement);
      for ( Element element : allElements )
      {
         processingEnvironment.getMessager().printMessage(
            Diagnostic.Kind.NOTE,
            "Element: " + element.getSimpleName(), element);
      }
   }

   /**
    * Use Elements.getAllAnnotationMirrors.
    * 
    * @param aElement Element overwhich all annotation mirrors are desired.
    */
   public void haveFunWithElementUtils(Element aElement)
   {
      processingEnvironment.getMessager().printMessage(
         Diagnostic.Kind.NOTE,
         "Entered haveFunWithElementUtils(Element) method." );
         
      Elements elements = processingEnvironment.getElementUtils();
      
      List<? extends AnnotationMirror> mirrors = elements.getAllAnnotationMirrors(aElement);
      for ( AnnotationMirror mirror : mirrors)
      {
         processingEnvironment.getMessager().printMessage(
            Diagnostic.Kind.NOTE,
            "Mirror: " + mirror.toString(),
            aElement,
            mirror );
      }
   }

   /**
    * Determine if passed-in element in an Entity ID.
    * 
    * @param aElement Element that may be an Entity ID.
    * @return true if passed-in element is Entity ID; false otherwise.
    */
   public boolean isElementAnEntityId(Element aElement)
   {
      boolean elementIsAnEntity = false;

      List <? extends AnnotationMirror> annotationMirrors =
         aElement.getAnnotationMirrors();
      for ( AnnotationMirror mirror : annotationMirrors )
      {
         if (mirror.getAnnotationType().toString().equals(javax.persistence.Id.class.getName()))
         {
            elementIsAnEntity = true;
         }
      }

      return elementIsAnEntity;
   }

   /**
    * Print generic information (erasure, capture, and element) of provided
    * Element that represents a Generic Type.
    * 
    * @param aElementWithGenericType Element with generic type.
    */
   public void printGenericTypeInformation(Element aElementWithGenericType)
   {
      final String erasure =
         processingEnvironment.getTypeUtils().erasure(
            aElementWithGenericType.asType()).toString();
      final String capture =
         processingEnvironment.getTypeUtils().capture(
            aElementWithGenericType.asType()).toString();
      final String elementStr =
         processingEnvironment.getTypeUtils().asElement(
            aElementWithGenericType.asType()).toString();
      final String outputStr =  "Erasure (" + erasure + "), Capture (" + capture
                              + "), Element (" + elementStr + ")";

      processingEnvironment.getMessager().printMessage(
         Diagnostic.Kind.NOTE, outputStr);  
   }

   /**
    * Get parameter type from provided generic collection.  JPA specification
    * (section 2.1.1) specifies three collection interface types that must be
    * supported here:
    * <ul>
    * <li>java.util.Collection</li>
    * <li>java.util.List</li>
    * <li>java.util.Map</li>
    * <li>java.util.Set</li>
    * </ul>
    * 
    * This method currently expects only Collection, List, or Set.  Support for
    * Map has not been implemented yet because it supports two parameterized
    * types (&lt;K,V&gt;) rather than a single parameterized type
    * (&lt;E&gt;) as expected here.
    * 
    * @param aElement Element representing generic with single parameterized type.
    * @return Parameter type from generic.
    */
   public String getGenericParameterType(Element aElement)
   {
      final String erasure =
         processingEnvironment.getTypeUtils().erasure(aElement.asType()).toString();
      final String capture =
         processingEnvironment.getTypeUtils().capture(aElement.asType()).toString();

      return trimParameterizedTypeBrackets(capture.substring(erasure.length()));
   }

   /**
    * Removes initial and final double quotation marks from provided String if
    * provided String does possess a double quote mark as its first and last
    * characters.
    *  
    * @param aStringWithQuotes
    * @return Provided String sans the initial and final double quote marks.
    */
   public String trimDoubleQuotes(final String aStringWithQuotes)
   {
      return trimQuotesOfDelimitingCharacters(aStringWithQuotes, '"');
   }
   
   public String trimParameterizedTypeBrackets(final String aStringWithBrackets)
   {
      final int origStringSize = aStringWithBrackets.length();
      if (    (origStringSize > 0)
           && (aStringWithBrackets.startsWith("<"))
           && (aStringWithBrackets.endsWith(">")) )
      {
         return aStringWithBrackets.substring(1,origStringSize-1);
      }
      else
      {
         return aStringWithBrackets;
      }   
   }
   
   public String trimQuotesOfDelimitingCharacters(final String aStringWithChars,
                                                  final Character aDelimitingChar)
   {
      final int origStringSize = aStringWithChars.length();
      if (    (origStringSize > 0)
           && (aStringWithChars.startsWith(aDelimitingChar.toString()))
           && (aStringWithChars.endsWith(aDelimitingChar.toString())) )
      {
         return aStringWithChars.substring(1,origStringSize-1);
      }
      else
      {
         return aStringWithChars;
      }      
   }
}
