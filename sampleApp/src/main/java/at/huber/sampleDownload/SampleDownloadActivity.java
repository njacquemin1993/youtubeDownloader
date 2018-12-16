package at.huber.sampleDownload;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.SparseArray;
import android.widget.Toast;

import at.huber.youtubeExtractor.VideoMeta;
import at.huber.youtubeExtractor.YouTubeExtractor;
import at.huber.youtubeExtractor.YtFile;

public class SampleDownloadActivity extends Activity {

    private static String youtubeLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check how it was started and if we can get the youtube link
        if (savedInstanceState == null && Intent.ACTION_SEND.equals(getIntent().getAction())
                && getIntent().getType() != null && "text/plain".equals(getIntent().getType())) {

            String ytLink = getIntent().getStringExtra(Intent.EXTRA_TEXT);

            if (ytLink != null
                    && (ytLink.contains("://youtu.be/") || ytLink.contains("youtube.com/watch?v="))) {
                youtubeLink = ytLink;
                // We have a valid link
                getYoutubeDownloadUrl(youtubeLink);
            } else {
                Toast.makeText(this, R.string.error_no_yt_link, Toast.LENGTH_LONG).show();
                finish();
            }
        } else if (savedInstanceState != null && youtubeLink != null) {
            getYoutubeDownloadUrl(youtubeLink);
        } else {
            finish();
        }
    }

    private void getYoutubeDownloadUrl(String youtubeLink) {
        new YouTubeExtractor(this) {

            @Override
            public void onExtractionComplete(SparseArray<YtFile> ytFiles, VideoMeta vMeta) {

                if (ytFiles == null) {
                    // Something went wrong we got no urls. Always check this.
                    finish();
                    return;
                }
                // Iterate over itags
                for (int i = 0, itag; i < ytFiles.size(); i++) {
                    itag = ytFiles.keyAt(i);
                    // ytFile represents one file with its url and meta data
                    YtFile ytFile = ytFiles.get(itag);

                    // Just add videos in a decent format => height -1 = audio
                    if (ytFile.getFormat().getHeight() == -1) {
                        String videoTitle = vMeta.getTitle();
                        String filename;
                        if (videoTitle.length() > 55) {
                            filename = videoTitle.substring(0, 55) + "." + ytFile.getFormat().getExt();
                        } else {
                            filename = videoTitle + "." + ytFile.getFormat().getExt();
                        }
                        filename = filename.replaceAll("[\\\\><\"|*?%:#/]", "");
                        downloadFromUrl(ytFile.getUrl(), videoTitle, filename);
                    }
                }
            }
        }.extract(youtubeLink, true, false);
    }

    private void downloadFromUrl(String youtubeDlUrl, String downloadTitle, String fileName) {
        Uri uri = Uri.parse(youtubeDlUrl);
        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setTitle(downloadTitle);

        request.allowScanningByMediaScanner();
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);

        DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        manager.enqueue(request);
    }

}
