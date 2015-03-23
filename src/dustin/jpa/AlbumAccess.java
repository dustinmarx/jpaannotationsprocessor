package dustin.jpa;

import java.util.List;

import java.lang.annotation.IncompleteAnnotationException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import javax.persistence.Query;


/**
 * Accesses information related to albums.
 */
public class AlbumAccess 
{
   /**
    * Constructor disabled (private) because methods are all accessed statically.
    */
   private AlbumAccess()
   {
   }

   /**
    * Retrieve Album information.
    */
   static public List<Album> queryAlbumsInfo(EntityManager aEm)
   {
      System.out.println("Entering AlbumAccess.queryAlbumsInfo");
      List<Album> albums = null;
      try
      {
         // TYPES OF JOINS (ALL USING "JOIN FETCH"):
         //
         // LEFT - 1. Shows all albums even if there is no genre associated
         //           with that particular album
         //        2. Shows multiple of each album with more than one genre, one
         //           per different associated genre
         //
         // LEFT OUTER - Same behavior as LEFT without OUTER.
         //
         // INNER - 1. Does not show album if no genre is associated with it.
         //         2. Shows multiple of each album with more than one genre,
         //            one per different associated genre
         //
         // (NONE) "JOIN FETCH" without LEFT or INNER behaves like INNER.
         //
         // For all of the above, the multiple returned albums for the same album
         // are all exactly the same, but repeated the number of times as there
         // are genres for that album.
         //
         Query query = aEm.createQuery("SELECT a FROM Album a LEFT JOIN FETCH a.genres");
         albums = (List<Album>) query.getResultList();
         for (Album album : albums)
         {
            System.err.println("Album: " + album);
         }
      }
      catch (Exception ex)
      {
         ex.printStackTrace();
      }
      return albums;
   }

   /**
    * Get all albums using named native query.
    * 
    * @param aEm Entity manager.
    * @return List of albums.
    */
   static public List <Album> getAllAlbumsWithNamedNativeQuery(EntityManager aEm)
   {
      Query query = aEm.createNamedQuery("findAllAlbums");
      return query.getResultList();
   }
  
   /**
    * Retrieve labels of all the available album ratings.
    * 
    * @param em EntityManager to use to query data source.
    * @return Labels of ratings available for album ratings.
    */
   static public List<Rating> queryRatingLabels(EntityManager em)
   {
      System.out.println("Entering queryRatingLabels(em) method");
      List<Rating> ratingLabels = null;
   
      try
      {
         Query query = em.createQuery("SELECT r FROM Rating r");
         ratingLabels = (List<Rating>) query.getResultList();
         System.out.println("Number of ratings: " + ratingLabels.size() );
         System.out.println("Ratings:");
         for (Rating label :ratingLabels)
         {
            System.out.println("\t" + label);
         }
      }
      catch (PersistenceException pEx)  // unchecked exception
      {
         System.out.println( "PersistenceException: " + pEx.getMessage() );
      }
      catch (IncompleteAnnotationException iaEx)  // unchecked exception
      {
         System.out.println("Annotation with Missing element: " + iaEx.annotationType() );
         System.out.println("Missing element: " + iaEx.elementName() );
      }
      catch (IllegalStateException isEx)  // unchecked exception
      {
         isEx.printStackTrace();
      }
      catch (Exception ex)
      {
         System.out.println( "Exception: " + ex.getMessage() );
      }
      finally
      {
      }

      return ratingLabels;
   }
   
   /**
    * Retrieve labels of all the available album genres.
    * 
    * @param em EntityManager to use to query data source.
    * @return Labels of genres available for album.
    */
   static public List<Genre> queryGenreLabels(EntityManager em)
   {
      System.out.println("Entering queryGenreLabels(em) method");
      List<Genre> genreLabels = null;
   
      try
      {
         Query query = em.createQuery("SELECT g FROM Genre g");
         genreLabels = (List<Genre>) query.getResultList();
         System.out.println("Number of genres: " + genreLabels.size() );
         System.out.println("Genres:");
         for (Genre label : genreLabels)
         {
            System.out.println("\t" + label);
         }
      }
      catch (PersistenceException pEx)  // unchecked exception
      {
         System.out.println( "PersistenceException: " + pEx.getMessage() );
      }
      catch (IncompleteAnnotationException iaEx)  // unchecked exception
      {
         System.out.println("Annotation with Missing element: " + iaEx.annotationType() );
         System.out.println("Missing element: " + iaEx.elementName() );
      }
      catch (IllegalStateException isEx)  // unchecked exception
      {
         isEx.printStackTrace();
      }
      catch (Exception ex)
      {
         System.out.println( "Exception: " + ex.getMessage() );
      }
      finally
      {
      }

      return genreLabels;
   }

   /**
    * Return all available Labels.
    * 
    * @param aEm EntityManager.
    */
   static public List<Label> queryLabels(EntityManager aEm)
   {
      System.out.println("Entering AlbumAccess.queryLabels(em) method");
      List<Label> labels = null;
   
      try
      {
         Query query = aEm.createQuery("SELECT l FROM Label l");
         labels = query.getResultList();
         System.out.println("Number of Labels: " + labels.size() );
         System.out.println("Labels:");
         for (Label label : labels)
         {
            System.out.println("\t" + label);
         }
      }
      catch (PersistenceException pEx)  // unchecked exception
      {
         System.out.println( "PersistenceException: " + pEx.getMessage() );
      }
      catch (IncompleteAnnotationException iaEx)  // unchecked exception
      {
         System.out.println("Annotation with Missing element: " + iaEx.annotationType() );
         System.out.println("Missing element: " + iaEx.elementName() );
      }
      catch (IllegalStateException isEx)  // unchecked exception
      {
         isEx.printStackTrace();
      }
      catch (Exception ex)
      {
         System.out.println( "Exception: " + ex.getMessage() );
         ex.printStackTrace();
      }
      finally
      {
      }

      return labels;
   }

   /**
    * Provides information on all artists in database.
    * 
    * @param aEm Entity Manager to use for database access.
    * @return The artists in the database.
    */
   public static List queryArtists(EntityManager aEm)
   {
      List <Artist> artists = null;
      Query query = aEm.createQuery("SELECT a FROM Artist a");
      artists = (List<Artist>) query.getResultList();
      System.out.println("Number of Artists: " + artists.size() );
      System.out.println("Artists: ");
      for (Artist artist : artists)
      {
         System.out.println("\t" + artist);
      }
      return artists;
   }
   
   /**
    * Add a new genre to the data store.  This method adds a genre both by
    * passing all arguments to the persistable object when it is constructed
    * and by constructing a persistable object with a no argument default
    * constructor and then using set methods to set the values.  Both ways
    * work as shown by this example.  To avoid constraint violations, the
    * second setting, that using set methods rather than passing the data to
    * the constructor of the persistable object, adds exclamation point (!) to
    * the end of each passed-in string for label and description.
    * 
    * @param aEm Entity Manager.
    * @param aLabel Label of the genre to be added.
    * @param aDescription Description of the genre.
    */
   public static void addGenre(EntityManager aEm, String aLabel, String aDescription)
   {
      try
      {
         Genre genreNoSets = new Genre(aLabel, aDescription);
         aEm.persist(genreNoSets);
         Genre genreUseSets = new Genre();
         genreUseSets.setLabel(aLabel + "!");
         genreUseSets.setDescription(aDescription + "!");
         aEm.persist(genreUseSets);
      }
      catch (Exception ex)
      {
         ex.printStackTrace();
      }
   }
   
   /**
    * Update an existing Genre to modify its description.
    * 
    * This method uses the JPA Query Language and the Query.executeUpdate(String)
    * method to execute the update.
    *
    * @param aEm Entity Manager.
    * @param aLabel Label of existing genre.
    * @param aDescription Description to set existing genre to use.
    */
   public static void modifyGenreDescription( EntityManager aEm,
                                              String aLabel,
                                              String aDescription )
   {
      StringBuffer updateString = new StringBuffer(100);
      updateString.append("UPDATE Genre g SET g.description = '");
      updateString.append(aDescription + "' WHERE ");
      updateString.append("g.label = '" + aLabel + "'");
      int numRowsUpdated = -1;
      try
      {
         Query update = aEm.createQuery(updateString.toString());
         numRowsUpdated = update.executeUpdate();
         System.out.println("#rows updated:" + numRowsUpdated);
      }
      catch (Exception ex)
      {
         ex.printStackTrace();
      }
   }
   
   /**
    * Modify existing Rating to have different description.
    * 
    * This method updates the Rating table/entity, but does so via a different
    * JPA approach than used to update genre description in the method
    * modifyGenreDescription().  In this method, a single entity is retrieved
    * with the EntityManager.find method that returns the Entity matching
    * the provided primary key value.  Then, that Entity is acted upon using
    * its "set" methods and the changed object is persisted with the
    * EntityManager.persist() method.  This is similar to creating a new
    * Entity and persisting it for an INSERT.
    * 
    * @param aEm Entity Manager.
    * @param aLabel Label of Rating to modify.
    * @param aRatingDescription
    */
   public static void modifyRatingDescription( EntityManager aEm,
                                               final String aLabel,
                                               final String aRatingDescription )
   {
      try
      {
         Rating rating = aEm.find(Rating.class, aLabel);
         if (rating == null) // no Entity with aLabel PK was found
         {
            System.err.println("No '" + aLabel + "' rating found!");
         }
         else
         {
            rating.setRatingDescription(aRatingDescription);
            aEm.persist(rating);
         }
      }
      catch (Exception ex)
      {
         ex.printStackTrace();
      }      
   }
   
   /**
    * Remove a Genre with the provided aLabel.
    *
    * @param aEm 
    * @param aLabel 
    */
   public static void removeGenreBasedOnLabel( EntityManager aEm,
                                               String aLabel )
   {
      StringBuffer removeString = new StringBuffer(100);
      removeString.append("DELETE FROM Genre g WHERE ");
      removeString.append("g.label = '" + aLabel + "'");
      int numRowsRemoved = -1;
      try
      {
         Query delete = aEm.createQuery(removeString.toString());
         numRowsRemoved = delete.executeUpdate();
         System.out.println("#rows deleted:" + numRowsRemoved);
      }
      catch (Exception ex)
      {
         ex.printStackTrace();
      }          
   }

   /**
    * Add a new director.
    */
   public static void addArtist(EntityManager aEm)
   {
      Artist artist = new Artist("Marx", "Dustin", Gender.MALE);
      aEm.persist(artist);
   }

   /**
    * Remove genre based on provided aLabel using the named parameter
    * approach.
    *
    * @param aEm 
    * @param aLabel 
    */
   public static void removeGenreWithNP( EntityManager aEm,
                                         String aLabel )
   {
      int numRowsRemoved = aEm.createQuery(
         "DELETE FROM Genre g WHERE g.label = :thelabel")
         .setParameter("thelabel", aLabel).executeUpdate();
      System.out.println("#rows removed: " + numRowsRemoved);
   }
   
   /**
    * Add a new rating with the provided label and age restrictions.
    * 
    * This method demonstrates executing a query directly on the database
    * without going through the normal JPA mapping.
    *
    * @param aEm Entity Manager.
    * @param aLabel Rating label.
    * @param aRatingDescription Rating description.
    * @param aNumberOfStars Number of stars associated with that rating.
    */
   public static void addRatingNQ( EntityManager aEm,
                                   String aLabel,
                                   String aRatingDescription,
                                   int aNumberOfStars )
   {
      StringBuffer insertStr = new StringBuffer(100);
      insertStr.append( "INSERT INTO rating VALUES ('" + aLabel );
      insertStr.append( "', '" + aRatingDescription + "', " );
      insertStr.append( aNumberOfStars + ")" );
      aEm.createNativeQuery(insertStr.toString()).executeUpdate();
   }

   /**
    * Removing Rating with the provided label (aLabel).
    * 
    * This method uses the NativeQuery approach to remove a rating.
    * 
    * @param aEm 
    * @param aLabel 
    */
   public static void removeRatingNQ( EntityManager aEm,
                                      String aLabel )
   {
      StringBuffer removeStr = new StringBuffer(100);
      removeStr.append("DELETE FROM rating WHERE label = '");
      removeStr.append(aLabel + "'");
      aEm.createNativeQuery(removeStr.toString()).executeUpdate();
   }
   
   /**
    * Get all ratings in the database using native query.  This uses the
    * createNativeQuery method and passes in a Rating.class to specify what
    * type of class to expect from the query.
    *
    * @param aEm Entity Manager.
    * @return List of all available albums ratings.
    */
   public static List getAllRatingsNQ( EntityManager aEm)
   {
      StringBuffer selectStr = new StringBuffer(100);
      selectStr.append("SELECT * FROM rating");
      return aEm.createNativeQuery(selectStr.toString(), Rating.class).getResultList();
   }
   
   /**
    * Get database date and time from specific database (Oracle).
    * 
    * Use NativeQuery approach here because required to run this vendor-specific
    * query.
    * 
    * @param aEm Entity Manager.
    */
   public static String getDatabaseSystemDateTime(EntityManager aEm)
   {
      String getDateString = "SELECT sysdate FROM dual";
      return aEm.createNativeQuery(getDateString).getSingleResult().toString();
   }
   
   /**
    * Obtain database date and time from specific database (Oracle) using
    * a NAMED native query.
    * 
    * Use NamedNativeQuery approach here because required to have native
    * for vendor-specific query and use named native query rather than passing
    * in SQL string.
    * 
    * @param aEm 
    * @return 
    */
   public static String getDatabaseSystemDateTimeNNQ(EntityManager aEm)
   {
      return aEm.createNamedQuery("oracle-getDbDate").getSingleResult().toString();
   }
}
