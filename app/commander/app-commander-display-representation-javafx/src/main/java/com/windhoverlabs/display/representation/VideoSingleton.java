package com.windhoverlabs.display.representation;

import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.base.MediaApi;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;

/**
 * FIXME: The way this is written at the moment it will only allow for one stream at a time. Will
 * have to implement an array of of the native objects and essentially manage the alloc/dealloc of
 * the objects, will have to be tied to the lifetime of Phoebus unfortunately...
 */
public class VideoSingleton {

  private MediaPlayerFactory mediaPlayerFactory;

  private EmbeddedMediaPlayer embeddedMediaPlayer;

  public EmbeddedMediaPlayer getEmbeddedMediaPlayer() {
    return embeddedMediaPlayer;
  }

  private static MediaApi videoMedia;

  // Step 1: Create a private static instance of the class (eager initialization)
  private static VideoSingleton instance = null;

  // Step 2: Make the constructor private so it cannot be instantiated from outside
  private VideoSingleton() {
    // Private constructor to prevent instantiation
  }

  // Step 3: Provide a public static method to return the instance
  public static VideoSingleton getInstance() {
    if (instance == null) {
      instance = new VideoSingleton();

      instance.mediaPlayerFactory = new MediaPlayerFactory();
      instance.embeddedMediaPlayer =
          instance.mediaPlayerFactory.mediaPlayers().newEmbeddedMediaPlayer();
      instance
          .embeddedMediaPlayer
          .events()
          .addMediaPlayerEventListener(
              new MediaPlayerEventAdapter() {
                @Override
                public void playing(MediaPlayer mediaPlayer) {}

                @Override
                public void paused(MediaPlayer mediaPlayer) {}

                @Override
                public void stopped(MediaPlayer mediaPlayer) {}

                @Override
                public void timeChanged(MediaPlayer mediaPlayer, long newTime) {}
              });
    }
    return instance;
  }

  // Example method to demonstrate singleton behavior
  public void playVideo() {
    System.out.println("Playing video...");
  }

  // Example method to demonstrate singleton behavior
  public void stopVideo() {
    System.out.println("Stopping video...");
  }
}
