package dustin.jpa;

import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import static javax.persistence.EnumType.STRING;
import static javax.persistence.GenerationType.SEQUENCE;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.SequenceGenerator;
import static javax.persistence.InheritanceType.SINGLE_TABLE;

@SequenceGenerator(name="INDIVIDUAL_SEQ", sequenceName="INDIVIDUAL_SEQ", allocationSize=1)

/**
 * Abstract entity class for any types of individuals.  Implementaton entities
 * might be things like ARTIST, PRODUCER, etc.
 */
@Entity
@Inheritance(strategy=SINGLE_TABLE)
@DiscriminatorColumn(name="ROLE")
// NOT specifying @DiscriminatorValue here because this class is abstract.
public abstract class Individual
{
   @Id
   @GeneratedValue(strategy=SEQUENCE, generator="INDIVIDUAL_SEQ")
   private Integer id;
   protected String last_name;
   protected String first_name;
   @Enumerated(STRING) protected Gender gender;

   public String toString()
   {
      StringBuffer sb = new StringBuffer();
      sb.append(first_name + " " + last_name + " (" + id + " - ");
      sb.append(gender.getMixedCaseForm() + ")");
      return sb.toString();
   }
}
