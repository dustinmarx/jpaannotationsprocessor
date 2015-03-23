package dustin.jpa;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
//@Table(name="INDIVIDUAL") // Don't need @Table here (inheritance of entity)
@DiscriminatorValue("ARTIST")
public class Artist extends Individual
{
   /**
    * Required (for a JPA entity) no-arg constructor.  Note, however, that
    * it can be private, as is this one, forcing clients of this persistable
    * entity to use the constructor that accepts arguments for the object to
    * be persisted (and thus do not need set methods).
    */
   private Artist()
   {
   }
   
   /**
    * Constructor accepting arguments.
    * 
    * @param aLastName Artist's surname.
    * @param aFirstName Artist's first name.
    * @param aGender Artist's gender.
    */
   public Artist(final String aLastName, final String aFirstName, final Gender aGender)
   {
      this.last_name = aLastName;
      this.first_name = aFirstName;
      this.gender = aGender;
   }
   
   /**
    * Provide this class's (Artist's) information in single String.
    * 
    * @return String representation of this Artist.
    */
   public String toString()
   {
      return "Artist: " + this.last_name + ", " + this.first_name;
   }
}
