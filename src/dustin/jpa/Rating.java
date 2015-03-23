package dustin.jpa;

import java.io.Serializable;

import javax.persistence.Column ;
import javax.persistence.Id;
import javax.persistence.Entity;

/**
 * Ratings for albums.
 */
@Entity (name="Rating")
public class Rating implements Serializable
{
   @Id private String label;
   private int num_stars;
   @Column(name="RATING_DESCRIPTION") private String ratingDescription;

   /**
    * Default no-arg constructor required for this class to be an entity.
    */
   public Rating()
   {
   }

   /**
    * Set my rating description.
    * 
    * @param aRatingDescription
    */
   public void setRatingDescription(String aRatingDescription)
   {
      this.ratingDescription = aRatingDescription;
   }

   /**
    * Present album Rating in String format.
    *
    * @return String representation of this class.
    */
   public String toString()
   {
      return this.label;
   }
}
