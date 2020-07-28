package power.audio.pro.music.player.model;

public class Constants {
    public interface PRIMARY_COLOR {
        int DARK = 1;
        int LIGHT = 2;
        int GLOSSY = 3;
    }
    public interface TYPEFACE {
        int MONOSPACE = 0;
        int SOFIA = 1;
        int MANROPE = 2;
        int ASAP = 3;
        int SYSTEM_DEFAULT = 4;
        int ACME = 5;
    }

    public static String TAG="beta.developer";
    public static String L_TAG = "Lyrics --";

    public interface SORT_ORDER{
        int ASC=0;
        int DESC=1;
    }

    public interface TABS {
        int ALBUMS = 0 ;
        int TRACKS = 1;
        int ARTIST = 2;
        int GENRE = 3;
        int PLAYLIST = 4;
        int FOLDER = 5;

        int NUMBER_OF_TABS = 6;
        String DEFAULT_SEQ="0,1,2,3,4,5";
    }

    public interface SORT_BY{
        int NAME = 0;
        int YEAR = 1;
        int NO_OF_ALBUMS = 2;
        int NO_OF_TRACKS = 3;
        int ASC = 4;
        int DESC = 5;
        int SIZE = 6;
        int DURATION = 7;

    }

}
