package dustin.jpa;

import static dustin.jpa.AlbumConstants.ALBUM_PU;

import java.lang.annotation.IncompleteAnnotationException;

import java.util.Iterator;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.PersistenceException;


/**
 * A test class demonstrating JPA with TopLink Essentials.  This class is
 * intended to show off JPA in a Java SE (Standard Edition) environment.
 */
public class StandardTest 
{
   private EntityManager em = null;

   /**
    * Default no-arg constructor.
    */
   public StandardTest()
   {
      System.out.println("Create EMF with persistence unit " + ALBUM_PU);
      EntityManagerFactory emf = Persistence.createEntityManagerFactory(ALBUM_PU);
      this.em = emf.createEntityManager();
   }

   /**
    * Get all information on all albums.
    */
  public void getAllAlbumsDetails()
  {
     log ("Entering getAllAlbumsDetails");
     EntityTransaction tx = null;
     try
     {
        tx = this.em.getTransaction();
        tx.begin();
        AlbumAccess.queryAlbumsInfo(this.em);
        tx.commit();
     }
     catch (Exception Ex)
     {
        Ex.printStackTrace();
     }
     finally
     {
        try
        {
           if ( tx == null )  // tx may be null; even isActive would not work
           {
              log ("No tranaction to handle.");
           }
           else if ( tx.isActive() )
           {
              log ("* * * * *  Active Transaction!  * * * * * ");
              tx.rollback();
           }
        }
        catch (Exception Ex2)
        {
           Ex2.printStackTrace();
        }
     }
  }
  
  /**
   * Tests using named native query to get all albums.
   */
  public void getAllAlbumsWithNamedNativeQuery()
  {
     List <Album> albums = AlbumAccess.getAllAlbumsWithNamedNativeQuery(this.em);
     System.out.println("Named Native Query Albums: " + albums);
     
     Iterator iter = albums.iterator();
     while (iter.hasNext())
     {
        System.out.println("Album (Native Query): " + iter.next().toString());
     }
     
     // With the enhanced for loop shown below, I was seeing the following
     // exception:
     //
     //   java.lang.ClassCastException: java.util.Vector
	  //     at marx.StandardTest.getAllAlbumsWithNamedNativeQuery(StandardTest.java:89)
	  //     at marx.StandardTest.main(StandardTest.java:439)
     /*
     for ( Album album : albums )
     {
        System.out.println("Album (Native Query): " + album.toString());
     }*/
  }

   /**
    * Retrieve all the available album ratings.
    */
   public void getAllAlbumRatings()
   {
      log("Entering getAllAlbumRatings()");
      EntityTransaction tx = null;
      try
      {
         tx =  this.em.getTransaction();
         tx.begin();
         List<Rating> ratings = AlbumAccess.queryRatingLabels(this.em);
         tx.commit();
      }
      catch (PersistenceException pEx)  // unchecked exception
      {
         System.out.println( "PersistenceException: " + pEx.getMessage() );
      }
      catch (IncompleteAnnotationException iaEx)  // unchecked exception
      {
         iaEx.printStackTrace();
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
         if ( tx != null && tx.isActive() )
         {
            tx.rollback();
         }
      }

      return;
   }
   
   public void getAllAlbumRatingsNQ()
   {
      List ratings = AlbumAccess.getAllRatingsNQ(this.em);
      System.out.println("RATINGS: " + ratings);
   }
   
   /**
    * Retrieve all album genres.
    */
   public List<Genre> getAllAlbumGenres()
   {
      log("Entering StandardTest.getAllAlbumGenres()");
      EntityTransaction tx = null;
      List<Genre> genres = null;
      try
      {
         tx =  this.em.getTransaction();
         tx.begin();
         genres = AlbumAccess.queryGenreLabels(this.em);
         tx.commit();
      }
      catch (PersistenceException pEx)  // unchecked exception
      {
         System.out.println( "PersistenceException: " + pEx.getMessage() );
      }
      catch (IncompleteAnnotationException iaEx)  // unchecked exception
      {
         iaEx.printStackTrace();
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
         if ( tx != null && tx.isActive() )
         {
            tx.rollback();
         }
      }

      return genres;
   }
   
   /**
    * 
    */
   public List<Label> getAllAlbumLabels()
   {
      log("Entering StandardTest.getAllAlbumLabels()");
      EntityTransaction tx = null;
      List<Label> labels = null;
      try
      {
         tx =  this.em.getTransaction();
         tx.begin();
         labels = AlbumAccess.queryLabels(this.em);
         tx.commit();
      }
      catch (PersistenceException pEx)  // unchecked exception
      {
         System.out.println( "PersistenceException: " + pEx.getMessage() );
      }
      catch (IncompleteAnnotationException iaEx)  // unchecked exception
      {
         iaEx.printStackTrace();
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
         if ( tx != null && tx.isActive() )
         {
            tx.rollback();
         }
      }

      return labels;
   }
   
   /**
    * 
    */
   public List<Artist> getAllAlbumArtists()
   {
      log("Entering StandardTest.getAllAlbumArtists()");
      EntityTransaction tx = null;
      tx = this.em.getTransaction();
      tx.begin();
      List<Artist> artists = AlbumAccess.queryArtists(this.em);
      tx.commit();
      return artists;
   }
   
   /**
    * 
    */
   public void addGenreTests()
   {
      log("Entering StandardTest.addGenreTests()");
      EntityTransaction tx = null;
      try
      {
         tx =  this.em.getTransaction();
         tx.begin();
         AlbumAccess.addGenre(this.em, "FOREIGN", "International");
         tx.commit();
      }
      catch(Exception ex)
      {
         ex.printStackTrace();
      }
      finally
      {
         if ( tx != null && tx.isActive() )
         {
            tx.rollback();
         }
      }
   }
   
   /**
    * 
    */
   public void modifyGenreTests()
   {
      log("Entering StandardTest.modifyGenreTests()");
      EntityTransaction tx = null;
      try
      {
         tx =  this.em.getTransaction();
         tx.begin();
         AlbumAccess.modifyGenreDescription(this.em, "FOREIGN!", "Other than U.S.");
         tx.commit();
      }
      catch(Exception ex)
      {
         ex.printStackTrace();
      }
      finally
      {
         if ( tx != null && tx.isActive() )
         {
            tx.rollback();
         }
      }
   }
   
   /**
    * 
    */
   public void modifyRatingTests()
   {
      log("Entering StandardTest.modifyRatingTests()");
      EntityTransaction tx = null;
      try
      {
         tx =  this.em.getTransaction();
         tx.begin();
         AlbumAccess.modifyRatingDescription(
            this.em,
            "Poor",
            "Not so good, but could be Awful or Worst");
         tx.commit();
      }
      catch(Exception ex)
      {
         ex.printStackTrace();
      }
      finally
      {
         if ( tx != null && tx.isActive() )
         {
            tx.rollback();
         }
      }     
   }
   
   /**
    * 
    */
   public void databaseSystemDateTest()
   {
      String systemDate = AlbumAccess.getDatabaseSystemDateTime(this.em);
      System.out.println("NQ - Database System Date: " + systemDate);
      
      systemDate = AlbumAccess.getDatabaseSystemDateTimeNNQ(this.em);
      System.out.println("NNQ - Database System Date: " + systemDate);
   }
   
   /**
    * Close resources that may still be open.
    */
   public void closeResources()
   {
      log("Entering closeResources()");
      if ( this.em != null && this.em.isOpen() )
      {
         this.em.close();
      }
   }
   
   /**
    * 
    */
   public void removeGenreTests()
   {
      log("Entering StandardTest.removeGenreTests()");
      EntityTransaction tx = null;
      try
      {
         tx =  this.em.getTransaction();
         tx.begin();
         AlbumAccess.removeGenreBasedOnLabel(this.em, "FOREIGN!");
         AlbumAccess.removeGenreWithNP(this.em, "FOREIGN");
         tx.commit();
      }
      catch (Exception ex)
      {
         ex.printStackTrace();
      }
   }
   
   public void addRatingTests()
   {
      EntityTransaction tx = null;
      tx =  this.em.getTransaction();
      tx.begin();      
      AlbumAccess.addRatingNQ(this.em, "Worst", "Get money back now", 0);
      tx.commit();
   }

   /**
    * 
    */
   public void addArtistTests()
   {
      EntityTransaction tx = null;
      tx = this.em.getTransaction();
      tx.begin();
      AlbumAccess.addArtist(this.em);
      tx.commit();
   }

   /**
    * 
    */
   public void removeRatingTests()
   {
      EntityTransaction tx = null;
      tx =  this.em.getTransaction();
      tx.begin();      
      AlbumAccess.removeRatingNQ(this.em, "Worst");
      tx.commit();      
   }
   
   /**
    * Primitive logging mechanism - meant to be replaced with more elaborate
    * logging.
    * 
    * @param mesageToLog The string/text that should be logged to console.
    */
   public void log( String messageToLog )
   {
      System.out.println(messageToLog);
   }

   /**
    * Main test driver to demonstrate JPA functionality.
    * 
    * @param args 
    */
   public static void main(String[] args)
   {
      StandardTest standardTest = new StandardTest();

      standardTest.getAllAlbumRatings();
      standardTest.getAllAlbumGenres();
      standardTest.getAllAlbumLabels();
      standardTest.getAllAlbumArtists();
      standardTest.getAllAlbumsDetails();
      standardTest.addGenreTests();
      standardTest.modifyGenreTests();
      standardTest.modifyRatingTests();
      standardTest.removeGenreTests();
      standardTest.addRatingTests();
      standardTest.removeRatingTests();
      standardTest.getAllAlbumRatingsNQ();
      standardTest.addArtistTests();
      standardTest.databaseSystemDateTest();
      standardTest.getAllAlbumsWithNamedNativeQuery();
      standardTest.closeResources();
   }
}
