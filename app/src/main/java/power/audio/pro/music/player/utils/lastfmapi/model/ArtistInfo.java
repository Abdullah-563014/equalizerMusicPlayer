package power.audio.pro.music.player.utils.lastfmapi.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ArtistInfo {
    @SerializedName("image")
    @Expose
    private List<Image> image = null;

    public List<Image> getImage() {
        return image;
    }

    public void setImage(List<Image> image) {
        this.image = image;
    }

    public Image getSmallImage() {
        return image == null ? null : image.get(0);
    }

    public Image getMediumImage() {
        return image == null ? null : image.get(1);
    }

    public Image getLargeImage() {
        return image == null ? null : image.get(2);
    }

    public Image getExtraLargeImage() {
        return image == null ? null : image.get(3);
    }

    public Image getMegaImage() {
        return image == null ? null : image.get(4);
    }

    public Image getOriginalImage() {
        return image == null ? null : image.get(5);
    }

    public Image getBestImage() {
        Image image = getOriginalImage();
        if (image != null) {
            return image;
        }
        image = getMegaImage();
        if (image != null) {
            return image;
        }
        image = getExtraLargeImage();
        if (image != null) {
            return image;
        }
        image = getLargeImage();
        if (image != null) {
            return image;
        }
        image = getMediumImage();
        if (image != null) {
            return image;
        }
        return getSmallImage();
    }
}
