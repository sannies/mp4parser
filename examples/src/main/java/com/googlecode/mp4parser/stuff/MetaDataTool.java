package com.googlecode.mp4parser.stuff;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import com.coremedia.iso.IsoFile;
import com.coremedia.iso.boxes.Box;
import com.coremedia.iso.boxes.ChunkOffsetBox;
import com.coremedia.iso.boxes.Container;
import com.coremedia.iso.boxes.MediaHeaderBox;
import com.coremedia.iso.boxes.MetaBox;
import com.coremedia.iso.boxes.MovieHeaderBox;
import com.coremedia.iso.boxes.StaticChunkOffsetBox;
import com.coremedia.iso.boxes.TrackHeaderBox;
import com.coremedia.iso.boxes.UnknownBox;
import com.coremedia.iso.boxes.UserDataBox;
import com.coremedia.iso.boxes.apple.AppleItemListBox;
import com.googlecode.mp4parser.boxes.apple.AppleGPSCoordinatesBox;
import com.googlecode.mp4parser.boxes.apple.AppleNameBox;
import com.googlecode.mp4parser.boxes.apple.Utf8AppleDataBox;
import com.googlecode.mp4parser.boxes.microsoft.XtraBox;
import com.googlecode.mp4parser.util.Path;

/**
 * Added by marwatk 3/1/15
 *
 */
public class MetaDataTool {
	public static final boolean DEBUG = true;
	public static void main(String[] args) {
		if( args.length != 7 && args.length != 1 ) {
			System.err.println( "Usage: java -jar metaDatTool.jar <inputFile> <outputFile> <title> <createDate> <userRating> <; separated tags> <gps coordinates>" );
			System.err.println( "  Use * for any value to keep the existing value, use an empty value to delete the current value" );
			System.err.println( "  Example: java -jar metaDataTool.jar myFile.mp4 newFile.mp4 \"New Title\" \"*\" 5 \"myTag 1;myTag 2\" \"\"" );
			System.err.println( "  This would retitle it, leave the create date alone, set the rating to 5 stars, " );
			System.err.println( "  replace any tags with 'myTag 1' and 'myTag 2' and delete the existing GPS coordinates" );
			System.err.println( "Other usage: java -jar metaDataToo.jar <inputFile>" );
			System.err.println( "  Prints a dump of all tags in the file" );
			System.exit( 1 );
		}
		
		if( args.length == 1 ) {
			MetaDataTool mdt;
			try {
				mdt = new MetaDataTool( args[0] );
				mdt.dumpBoxes();
				System.exit( 0 );
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		int i = 0;
		String inFile = args[i++];
		String outFile = args[i++];
		String title = args[i++];
		String createDate = args[i++];
		String userRating = args[i++];
		String tags = args[i++];
		String gpsCoords = args[i++];
		
		try {
			System.out.println( "================= BEFORE ===================" );
			MetaDataTool mdt = new MetaDataTool( inFile );
			mdt.dumpBoxes();
			if( !"*".equals( title ) ) {
				mdt.setTitle( title );
			}
			if( !"*".equals( createDate ) ) {
				Date inputDate = parseDate( createDate );
				mdt.setMediaCreateDate( inputDate );
				mdt.setMediaModificationDate( inputDate );
			}
			if( !"*".equals( userRating ) ) {
				if( "".equals( userRating ) ) {
					mdt.removeWindowsMediaTag( WM_RATING_TAG );
				}
				else {
					mdt.setWindowsMediaRating( Integer.valueOf( userRating ) );
				}
			}
			if( !"*".equals( tags ) ) {
				if( "".equals( tags ) ) {
					mdt.removeWindowsMediaTag( WM_TAGS_TAG );
				}
				else {
					String tagsAr[] = tags.split( ";" );
					mdt.setWindowsMediaTags( tagsAr );
				}
			}
			if( !"*".equals( gpsCoords ) ) {
				mdt.setGpsCoordinates( gpsCoords );
			}
			mdt.writeMp4( outFile );
			if( DEBUG ) {
				mdt = new MetaDataTool( outFile );
				System.out.println( "================= AFTER ===================" );
				mdt.dumpBoxes();
			}
		}
		catch( Exception e ) {
			e.printStackTrace();
		}
	}
	
	private long originalUserDataSize = 0;
	private XtraBox xtraBox;
	private UserDataBox userDataBox;
	private MetaBox metaBox;
	private IsoFile isoFile;
	
	public MetaDataTool( String path ) throws IOException {
		
		//The source I copied this from created 2 new files, a temp file and a target file
		//I'm not sure this is necessary, but maybe when you make changes it's edited in-place?
		//Anyway, just to be safe I'm keeping it so no operations are done on original file
        File videoFile = new File(path);
        if (!videoFile.exists())
            throw new FileNotFoundException("File " + path + " not exists");

        if (!videoFile.canWrite())
            throw new IllegalStateException("No write permissions to file " + path);

        File tempFile = File.createTempFile("ChangeMetaData", "");
        FileUtils.copyFile(videoFile, tempFile);
        tempFile.deleteOnExit();

        isoFile = new IsoFile(tempFile.getAbsolutePath());
        userDataBox = Path.getPath(isoFile, "/moov/udta");
        if( userDataBox != null ) {
        	originalUserDataSize = userDataBox.getSize();
        }
        
	}
	
	public static final String WM_RATING_TAG = "WM/SharedUserRating";
	public static final int WM_RATING_VALS[] = { 0, 1, 25, 50, 75, 99 };
	public void setWindowsMediaRating( int rating ) { //0-5
		if( rating < 0 || rating > 5 ) {
			throw new RuntimeException( "Invalid rating, 0-5 only" );
		}
		
		if( rating == 0 ) {
			removeWindowsMediaTag( WM_RATING_TAG );
		}
		else {
			setWindowsMediaLong( WM_RATING_TAG, WM_RATING_VALS[rating] );
		}
	}
	
	public static final String WM_TAGS_TAG = "WM/Category";
	public void setWindowsMediaTags( String tags[] ) {
		if( tags == null || tags.length == 0 ) {
			removeWindowsMediaTag( WM_TAGS_TAG );
		}
		else {
			setWindowsMediaStrings( WM_TAGS_TAG, tags );
		}
	}
	
	private void setMediaDate( Date date, boolean create ) {
		List<Box> headers = getBoxes( isoFile, new String[] { MovieHeaderBox.TYPE, MediaHeaderBox.TYPE, TrackHeaderBox.TYPE } );
		boolean set = false;
		for( Box header : headers ) {
			if( header instanceof MediaHeaderBox ) {
				set = true;
				if( create ) {
					((MediaHeaderBox)header).setCreationTime( date );
				}
				else {
					((MediaHeaderBox)header).setModificationTime( date );
				}
			}
			else if( header instanceof MovieHeaderBox ) {
				set = true;
				if( create ) {
					((MovieHeaderBox)header).setCreationTime(date);
				}
				else {
					((MovieHeaderBox)header).setModificationTime(date);
				}
			}
			else if( header instanceof TrackHeaderBox ) {
				set = true;
				if( create ) {
					((TrackHeaderBox)header).setCreationTime(date);
				}
				else {
					((TrackHeaderBox)header).setModificationTime(date);
				}
				
			}
		}
		setWindowsMediaDate( "WM/EncodingTime", date );
		if( !set ) {
			throw new RuntimeException( "Can't yet add MovieHeaderBox or MediaHeaderBox and none were preset to set create and/or modify date" );
		}
	}
	
	public void setGpsCoordinates( String iso6709String ) {
		AppleGPSCoordinatesBox coordBox = (AppleGPSCoordinatesBox)getBox( isoFile, AppleGPSCoordinatesBox.TYPE );
		if( coordBox == null ) {
			UserDataBox udb = getUserDataBox();
			coordBox = new AppleGPSCoordinatesBox();
			udb.addBox( coordBox );
		}
		coordBox.setValue( iso6709String );
	}
	
	public void setMediaCreateDate( Date date ) {
		setMediaDate( date, true );
	}
	public void setMediaModificationDate( Date date ) {
		setMediaDate( date, false );
	}
	
	public void setTitle( String title ) {
		AppleNameBox titleBox = (AppleNameBox)getBox( isoFile, AppleNameBox.TYPE );
		if( titleBox == null ) {
			AppleItemListBox itemList = getItemListBox();
			titleBox = new AppleNameBox();
			itemList.addBox( titleBox );
		}
		titleBox.setValue( title );
	}
	
	private AppleItemListBox getItemListBox() {
		AppleItemListBox itemList = (AppleItemListBox)getBox( isoFile, AppleItemListBox.TYPE );
		if( itemList == null ) {
			MetaBox mb = getMetaBox();
			itemList = new AppleItemListBox();
			mb.addBox( itemList );
		}
		return itemList;
	}
	
	@SuppressWarnings("deprecation")
	public void setMediaModificationDate( String date ) {
		setMediaModificationDate( new Date( Date.parse( date ) ) ); //Deprecated, but also the easiest way to do this quickly
	}
	@SuppressWarnings("deprecation")
	public void setMediaCreateDate( String date ) {
		try {
			setMediaCreateDate( new Date( Date.parse( date ) ) ); //Deprecated, but also the easiest way to do this quickly
		} catch( IllegalArgumentException e ) {
			throw new RuntimeException( "Unable to parse date '" + date + "'", e );
		}
	}
	
	public void setWindowsMediaDate( String tagName, Date dateVal ) {
		XtraBox xb = getXtraBox();
		xb.setTagValue( tagName, dateVal );
	}

	public void setWindowsMediaLong( String tagName, long longVal ) {
		XtraBox xb = getXtraBox();
		xb.setTagValue( tagName, longVal );
	}
	
	public void setWindowsMediaStrings( String tagName, String values[] ) {
		XtraBox xb = getXtraBox();
		xb.setTagValues( tagName, values );
	}
	
	public void removeWindowsMediaTag( String tagName ) {
		XtraBox xb = getXtraBox();
		xb.removeTag( tagName );
	}
	

	private UserDataBox getUserDataBox() {
		if( userDataBox == null ) {
			userDataBox = new UserDataBox();
			isoFile.getMovieBox().addBox( userDataBox );
		}
		return userDataBox;
	}
	
	private MetaBox getMetaBox() {
		if( metaBox == null ) {
			UserDataBox ud = getUserDataBox();
			metaBox = (MetaBox) getBox( ud, MetaBox.TYPE );
			if( metaBox == null ) {
				metaBox = new MetaBox();
                ud.addBox(metaBox);
			}
		}
		return metaBox;
	}
	
	private XtraBox getXtraBox() {
		if( xtraBox == null ) {
			UserDataBox ud = getUserDataBox(); //Create user data box if necessary
			xtraBox = (XtraBox)getBox( ud, XtraBox.TYPE );
			if( xtraBox == null ) {
				xtraBox = new XtraBox();
				ud.addBox( xtraBox );
			}
		}
		return xtraBox;
	}
	
	public void writeMp4( String filename ) throws IOException {
        long finalUserDataSize = 0;
		if( userDataBox != null ) {
			finalUserDataSize = userDataBox.getSize();
		}
        if (needsOffsetCorrection(isoFile)) {
            correctChunkOffsets(isoFile, finalUserDataSize - originalUserDataSize);
        }
        FileOutputStream videoFileOutputStream = null;
        try {
	        videoFileOutputStream = new FileOutputStream(filename);
	        isoFile.getBox(videoFileOutputStream.getChannel());
        }
        finally {
        	closeQuietly(isoFile);
        	IOUtils.closeQuietly(videoFileOutputStream);
        }
	}
	
	private static String getIndentation( int indent ) {
		char c[] = new char[indent];
		for( int i = 0; i < indent; i++ ) {
			c[i] = ' ';
		}
		return new String( c );
	}
	
	public void dumpBoxes() {
		dumpBoxes( isoFile, 0 );
	}
	
	private static void dumpBoxes( Container container, int indent ) {
		String meInd = getIndentation( indent );
		String subInd = getIndentation( indent + 2 );
		System.out.println( meInd +  container.getClass().getName() );
		for (Box box : container.getBoxes() ) {
        	if( box instanceof Container ) {
        		dumpBoxes( (Container)box, indent + 2 );
        	}
        	else {
        		try {
	        		if( box instanceof UnknownBox ) {
	        			System.out.println( subInd + box.getClass().getName() + "[" + box.getSize() + "/" + box.getType() + "]:" + box.toString() );
	        		}
		        	else if( box instanceof Utf8AppleDataBox ) {
	        			System.out.println( subInd + box.getClass().getName() + ": " + box.getType() + ": " + box.toString() + ": " + ((Utf8AppleDataBox)box).getValue() );
		        	}
		        	else {
	        			System.out.println( subInd + box.getClass().getName() + ": " + box.getType() + "[" + box.getSize() + "]: " + box.toString() );
		        	}
        		}
        		catch( Exception e ) {
        			System.err.println( "Error parsing " + box.getClass().getSimpleName() + " box: " + e );
        			e.printStackTrace( System.err );
        		}
    		}
        }
	}

    public static boolean needsOffsetCorrection(IsoFile isoFile) {

        if (Path.getPaths(isoFile, "mdat").size() > 1) {
            throw new RuntimeException("There might be the weird case that a file has two mdats. One before" +
                    " moov and one after moov. That would need special handling therefore I just throw an " +
                    "exception here. ");
        }

        if (Path.getPaths(isoFile, "moof").size() > 0) {
            throw new RuntimeException("Fragmented MP4 files need correction, too. (But I would need to look where)");
        }

        for (Box box : isoFile.getBoxes()) {
            if ("mdat".equals(box.getType())) {
                return false;
            }
            if ("moov".equals(box.getType())) {
                return true;
            }
        }
        throw new RuntimeException("Hmmm - shouldn't happen");
    }

    private static void correctChunkOffsets(IsoFile tempIsoFile, long correction) {
        List<Box> chunkOffsetBoxes = Path.getPaths(tempIsoFile, "/moov[0]/trak/mdia[0]/minf[0]/stbl[0]/stco[0]");
        for (Box chunkOffsetBox : chunkOffsetBoxes) {

            LinkedList<Box> stblChildren = new LinkedList<Box>(chunkOffsetBox.getParent().getBoxes());
            stblChildren.remove(chunkOffsetBox);

            long[] cOffsets = ((ChunkOffsetBox) chunkOffsetBox).getChunkOffsets();
            for (int i = 0; i < cOffsets.length; i++) {
                cOffsets[i] += correction;
            }

            StaticChunkOffsetBox cob = new StaticChunkOffsetBox();
            cob.setChunkOffsets(cOffsets);
            stblChildren.add(cob);
            chunkOffsetBox.getParent().setBoxes(stblChildren);
        }
    }
    public static void deleteQuietly( File f ) {
    	try {
    		f.delete();
    	}
    	catch( Exception ioe ) {
    		//ignore
    	}
    }
    public static void closeQuietly(IsoFile input) {
        try {
            if (input != null) {
                input.close();
            }
        } catch (IOException ioe) {
            // ignore
        }
    }
    public static Box getBox( Container outer, String type ) {
    	List<Box> list = getBoxes( outer, new String[] { type } );
    	return list.get( 0 );
    }
    public static List<Box> getBoxes( Container outer, String types[], List<Box> list ) {
		for (Box box : outer.getBoxes() ) {
        	for( int i = 0; i < types.length; i++ ) {
        		if( box.getType().equals( types[i] ) ) {
        			list.add( box );
        		}
        	}
			if( box instanceof Container ) {
        		getBoxes( (Container)box, types, list );
        	}
		}
		return list;
    }
    
    public static List<Box> getBoxes( Container outer, String types[] ) {
		List<Box> list = new ArrayList<Box>();
    	return getBoxes( outer, types, list );
    }
    	
    

    //http://stackoverflow.com/questions/3389348/parse-any-date-in-java
    private static final HashMap<String, String> DATE_FORMAT_REGEXPS = new HashMap<String, String>() { {
        put("^\\d{8}$", "yyyyMMdd");
        put("^\\d{1,2}-\\d{1,2}-\\d{4}$", "dd-MM-yyyy");
        put("^\\d{4}-\\d{1,2}-\\d{1,2}$", "yyyy-MM-dd");
        put("^\\d{1,2}/\\d{1,2}/\\d{4}$", "MM/dd/yyyy");
        put("^\\d{4}/\\d{1,2}/\\d{1,2}$", "yyyy/MM/dd");
        put("^\\d{1,2}\\s[a-z]{3}\\s\\d{4}$", "dd MMM yyyy");
        put("^\\d{1,2}\\s[a-z]{4,}\\s\\d{4}$", "dd MMMM yyyy");
        put("^\\d{12}$", "yyyyMMddHHmm");
        put("^\\d{8}\\s\\d{4}$", "yyyyMMdd HHmm");
        put("^\\d{1,2}-\\d{1,2}-\\d{4}\\s\\d{1,2}:\\d{2}$", "dd-MM-yyyy HH:mm");
        put("^\\d{4}-\\d{1,2}-\\d{1,2}\\s\\d{1,2}:\\d{2}$", "yyyy-MM-dd HH:mm");
        put("^\\d{1,2}/\\d{1,2}/\\d{4}\\s\\d{1,2}:\\d{2}$", "MM/dd/yyyy HH:mm");
        put("^\\d{4}/\\d{1,2}/\\d{1,2}\\s\\d{1,2}:\\d{2}$", "yyyy/MM/dd HH:mm");
        put("^\\d{1,2}\\s[a-z]{3}\\s\\d{4}\\s\\d{1,2}:\\d{2}$", "dd MMM yyyy HH:mm");
        put("^\\d{1,2}\\s[a-z]{4,}\\s\\d{4}\\s\\d{1,2}:\\d{2}$", "dd MMMM yyyy HH:mm");
        put("^\\d{14}$", "yyyyMMddHHmmss");
        put("^\\d{8}\\s\\d{6}$", "yyyyMMdd HHmmss");
        put("^\\d{1,2}-\\d{1,2}-\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}$", "dd-MM-yyyy HH:mm:ss");
        put("^\\d{4}-\\d{1,2}-\\d{1,2}\\s\\d{1,2}:\\d{2}:\\d{2}$", "yyyy-MM-dd HH:mm:ss");
        put("^\\d{1,2}/\\d{1,2}/\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}$", "MM/dd/yyyy HH:mm:ss");
        put("^\\d{4}/\\d{1,2}/\\d{1,2}\\s\\d{1,2}:\\d{2}:\\d{2}$", "yyyy/MM/dd HH:mm:ss");
        put("^\\d{1,2}\\s[a-z]{3}\\s\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}$", "dd MMM yyyy HH:mm:ss");
        put("^\\d{1,2}\\s[a-z]{4,}\\s\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}$", "dd MMMM yyyy HH:mm:ss");
    } };
    
    public static String determineDateFormat(String dateString) {
        for (String regexp : DATE_FORMAT_REGEXPS.keySet()) {
            if (dateString.toLowerCase().matches(regexp)) {
                return DATE_FORMAT_REGEXPS.get(regexp);
            }
        }
        return null; // Unknown format.
    }
    
    public static Date parseDate( String dateString ) throws ParseException {
    	String formatString = determineDateFormat( dateString );
    	if( formatString == null ) {
    		return null;
    	}
    	SimpleDateFormat sdf = new SimpleDateFormat( formatString );
    	return sdf.parse( dateString );
    }
    
}
