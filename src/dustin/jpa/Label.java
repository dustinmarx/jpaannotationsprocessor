package dustin.jpa;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Album label.
 */
@Entity (name="Label")
public class Label 
{
   /**
    * Default constructor required for JPA entity.
    */
   public Label()
   {
   }
   
   /**
    * Label's string representation.
    * 
    * @return Label's string representation.
    */
   public String toString()
   {
      return this.id;
   }
   
   @Id private String id;
}
