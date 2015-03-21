-- populateAlbum.sql
--
-- Populate Album database for testing.
--

-- INSERT standard RATING values to rate albums
INSERT INTO rating VALUES ( 'Awful', 'Absolutely Terrible', 1 );
INSERT INTO rating VALUES ( 'Poor', 'Not so Good', 2 );
INSERT INTO rating VALUES ( 'Fair', 'Decent, worth listening to', 3 );
INSERT INTO rating VALUES ( 'Good', 'Well worth listening to', 4 );
INSERT INTO rating VALUES ( 'Best', 'The must-have albums', 5 );

-- INSERT standard music genres for classification of albums
INSERT INTO genre VALUES ( 'Classical',
	                   'Bach, Beethoven, Mozart and the like' );
INSERT INTO genre VALUES ( 'Soft Rock',
	                   'Softer than normal rock; includes Pop' );
INSERT INTO genre VALUES ( 'Rock', 'Rock and Roll' );
INSERT INTO genre VALUES ( 'Hard Rock',
	                   'Harder than normal rock; includes Metal' );
INSERT INTO genre VALUES ( 'Country', 'Old and modern country' );
INSERT INTO genre VALUES ( 'Children', 'Oriented to children' );

-- INSERT standard roles related to music and albums
INSERT INTO role VALUES ( 'ARTIST' );
INSERT INTO role VALUES ( 'WRITER' );
INSERT INTO role VALUES ( 'PRODUCER' );

-- INSERT the label that is producing/distributing the albums
INSERT INTO label VALUES ( 'Marx Music Madness' );
INSERT INTO label VALUES ( 'Dustin''s Determined Devas' );
INSERT INTO label VALUES ( 'Music is Me' );

-- INSERT the artists
INSERT INTO individual VALUES ( individual_seq.NEXTVAL,
	                        'Alpha', 'Arrow', 'ARTIST', 'MALE' );
INSERT INTO individual VALUES ( individual_seq.NEXTVAL,
	                        'Beta', 'Blocker', 'ARTIST', 'FEMALE' );

-- INSERT the albums themselves
INSERT INTO album VALUES ( 1,
	                   'A First Album', 'Band One', 2007,
                           'The first album in the collection',
                           'Marx Music Madness', 'Good' );
INSERT INTO album VALUES ( 2,
	                   'A Second Album', 'Band Two', 2005,
	                   'The second album in the collection',
			   'Music is Me', 'Fair' );
INSERT INTO album VALUES ( 3,
	                   'The Third Album', 'Triple Trio', 2001,
	                   'Musical Encounters of the Third Kind',
			   'Dustin''s Determined Devas', 'Best' );
INSERT INTO album VALUES ( 4,
	                   'Horrible Music Sampler', 'Various', 1989,
	                   'I do not know why I bought this!',
			   'Music is Me', 'Awful' );

COMMIT;
