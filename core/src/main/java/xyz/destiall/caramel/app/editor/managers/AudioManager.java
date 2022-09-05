package xyz.destiall.caramel.app.editor.managers;

import caramel.api.audio.AudioPlayer;
import caramel.api.events.SceneStopEvent;
import caramel.api.objects.GameObject;
import xyz.destiall.java.events.EventHandler;
import xyz.destiall.java.events.Listener;

import java.util.Set;

public final class AudioManager implements Listener {

    @EventHandler
    private void onStopScene(final SceneStopEvent e) {
        for (final GameObject gameObject : e.getScene().getGameObjects()) {
            final Set<AudioPlayer> audioPlayers = gameObject.getComponents(AudioPlayer.class);
            for (final AudioPlayer a : audioPlayers) {
                a.invalidate();
            }
        }
    }
}
