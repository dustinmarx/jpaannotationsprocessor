package dustin.jpa;

import java.io.Serializable;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;

/**
 * Album genre or type of music the album most appropriately
 * falls within.
 */
@Entity (name="Genre")
public class Genre implements Serializable
{
   @Id private String label;
   private String description;
   @ManyToMany(mappedBy="genres") private Set<Album> albums;

   /**
    * No-arg default constructor required for this to be Entity.
    */
   public Genre()
   {
   }
   
   /**
    * Alternative constructor accepting label and description strings.
    *
    * @param aLabel Genre label/name/title.
    * @param aDescription Genre text description.
    */
   public Genre(String aLabel, String aDescription)
   {
      this.label = aLabel;
      this.description = aDescription;
   }
   
   /**
    * Set label/name of the genre.
    * 
    * @param aLabel 
    */
   public void setLabel(String aLabel)
   {
      this.label = aLabel;
   }
   
   /**
    * Set text description of the genre.
    * 
    * @param aDescription 
    */
   public void setDescription(String aDescription)
   {
      this.description = aDescription;
   }

   /**
    * Present String representation of this Genre.
    *
    * @return String representation of this genre.
    */
   public String toString()
   {
      return this.label;
   }
}
