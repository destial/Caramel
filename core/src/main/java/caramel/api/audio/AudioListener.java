package caramel.api.audio;

import caramel.api.events.SceneStopEvent;
import caramel.api.objects.GameObject;
import xyz.destiall.java.events.EventHandler;
import xyz.destiall.java.events.Listener;

import java.util.Set;

public final class AudioListener implements Listener {

    @EventHandler
    private void onStopScene(SceneStopEvent e) {
        for (GameObject gameObject : e.getScene().getGameObjects()) {
            Set<AudioPlayer> audioPlayers = gameObject.getComponentsInChildren(AudioPlayer.class);
            for (AudioPlayer a : audioPlayers) {
                a.stop();
            }
        }
    }
}
