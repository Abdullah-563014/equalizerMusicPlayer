package power.audio.pro.music.player.utils;

import android.util.SparseArray;

import java.util.ArrayList;

public class PlayerNotificationManager {
    private static int totalEvents = 1;

    public static final int updateTime = totalEvents++;
    public static final int completion = totalEvents++;
    public static final int error = totalEvents++;
    public static final int start = totalEvents++;
    public static final int pause = totalEvents++;
    public static final int shuffle = totalEvents++;
    public static final int repeat = totalEvents++;
    public static final int info = totalEvents++;
    public static final int swapSong = totalEvents++;
    public static final int toggleFavorite = totalEvents++;
    public static final int removeSongFromQueue = totalEvents++;
    public static final int addSongToQueue = totalEvents++;
    public static final int deleteSong = totalEvents++;
    public static final int removeSongFromPlaylist = totalEvents++;
    public static final int addSongsToPlaylist = totalEvents++;
    public static final int playlistEmpty = totalEvents++;

    private static SparseArray<ArrayList<PlayerNotificationCenterDelegate>> observers = new SparseArray<>();
    private static SparseArray<ArrayList<PlayerNotificationCenterDelegate>> removeAfterBroadcast = new SparseArray<>();
    private static SparseArray<ArrayList<PlayerNotificationCenterDelegate>> addAfterBroadcast = new SparseArray<>();

    private static int broadcasting = 0;

    public interface PlayerNotificationCenterDelegate {
        void onReceivedPlayerNotification(int id, Object... args);
    }

    public static void postNotificationName(int id, Object... args) {
        broadcasting++;
        ArrayList<PlayerNotificationCenterDelegate> observer = observers.get(id);
        if (observer != null && !observer.isEmpty()) {
            for (PlayerNotificationCenterDelegate ncd : observer) {
                ncd.onReceivedPlayerNotification(id, args);
            }
        }
        broadcasting--;
        if (broadcasting == 0) {
            if (removeAfterBroadcast.size() != 0) {
                for (int a = 0; a < removeAfterBroadcast.size(); a++) {
                    int key = removeAfterBroadcast.keyAt(a);
                    ArrayList<PlayerNotificationCenterDelegate> arrayList = removeAfterBroadcast.get(key);
                    for (PlayerNotificationCenterDelegate ncd : arrayList) {
                        removeObserver(ncd, key);
                    }
                }
                removeAfterBroadcast.clear();
            }
            if (addAfterBroadcast.size() != 0) {
                for (int a = 0; a < addAfterBroadcast.size(); a++) {
                    int key = addAfterBroadcast.keyAt(a);
                    ArrayList<PlayerNotificationCenterDelegate> arrayList = addAfterBroadcast.get(key);
                    for (PlayerNotificationCenterDelegate ncd : arrayList) {
                        addObserver(ncd, key);
                    }
                }
                addAfterBroadcast.clear();
            }
        }
    }

    public static void addObserver(PlayerNotificationCenterDelegate observer, int id) {
        if (broadcasting != 0) {
            ArrayList<PlayerNotificationCenterDelegate> arrayList = addAfterBroadcast.get(id);
            if (arrayList == null) {
                arrayList = new ArrayList<>();
                addAfterBroadcast.put(id, arrayList);
            }
            arrayList.add(observer);
            return;
        }
        ArrayList<PlayerNotificationCenterDelegate> objects = observers.get(id);
        if (objects == null) {
            observers.put(id, (objects = new ArrayList<>()));
        }
        if (objects.contains(observer)) {
            return;
        }
        objects.add(observer);
    }

    public static void removeObserver(PlayerNotificationCenterDelegate observer, int id) {
        if (broadcasting != 0) {
            ArrayList<PlayerNotificationCenterDelegate> arrayList = removeAfterBroadcast.get(id);
            if (arrayList == null) {
                arrayList = new ArrayList<>();
                removeAfterBroadcast.put(id, arrayList);
            }
            arrayList.add(observer);
            return;
        }
        observers.get(id).remove(observer);
    }

    public static boolean isEmptyObservers() {
        if (observers.size() == 0) {
            return true;
        }

        for (int i = 0; i < observers.size(); i++) {
            if (!observers.valueAt(i).isEmpty()) {
                return false;
            }
        }

        return true;
    }
}
