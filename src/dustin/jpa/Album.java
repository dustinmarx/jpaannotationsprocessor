package dustin.jpa;

import java.io.Serializable;
import java.util.Set;

import javax.persistence.Column ;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.NamedNativeQueries;
import javax.persistence.NamedNativeQuery;
import javax.persistence.Transient;

import static dustin.jpa.AlbumConstants.*;

/**
 * Class for storing album information.
 */
@Entity // used default of following class name
@NamedNativeQueries({
   @NamedNativeQuery(
       name="findAllAlbums",
       query="SELECT * FROM ALBUM"
   ),
   @NamedNativeQuery(
       name="oracle-getDbDate",
       query="SELECT sysdate FROM dual"
   )
})
public class Album implements Serializable
{
   @Id private Integer id;
   private String title;
   @Column(name="band_name") private String bandName;
   private int year;
   private String description;
   
   // This member did not need any @JoinColumn designation because the
   // JPA default, concatenating the name of the joined table to the name of
   // the referenced column in that table (RATING and LABEL in this case)
   // matches the name of this table's (ALBUM) column that points to that
   // column.
   private Rating rating;

   @ManyToMany
   @JoinTable(
      name="album_genre",
      joinColumns=@JoinColumn(name="album_id", referencedColumnName="id"),
      inverseJoinColumns=@JoinColumn(name="genre_label", referencedColumnName="label")
   )
   private Set<Genre> genres;
   
   // Need to specify alternate name for the Album's relationship to
   // Label because the foreign key in Album is called "Label" and JPA
   // default setting is "Label_Id".
   @JoinColumn(name="Label") private Label label;
   
   @Transient private String somethingNotPersisted;
   
   /**
    * Default no-argument constructor necessary for Java class being used as
    * an entity.
    */
   public Album()
   {
   }
   
   /**
    * Provide string representation of my contents.
    * 
    * @return My contents in String format.
    */
   public String toString()
   {
      StringBuffer sb = new StringBuffer(INITIAL_BUFFER_SIZE_TO_STRING);
      sb.append("{ALBUM ID: " + this.id);
      sb.append("; TITLE: " + this.title);
      sb.append("; YEAR: " + this.year);
      sb.append("; DESCRIPTION: " + this.description);
      sb.append("; RATING: " + this.rating);
      if (this.genres.size() < 1)
      {
         sb.append("}");
      }
      for (Genre genre : this.genres)
      {
         sb.append("; GENRE: " + genre + "}");
      }
      return sb.toString();
   }
}
