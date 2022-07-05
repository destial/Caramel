package xyz.destiall.caramel.api.audio;

import xyz.destiall.caramel.api.Component;
import xyz.destiall.caramel.api.interfaces.FunctionButton;
import xyz.destiall.caramel.api.objects.GameObject;
import xyz.destiall.caramel.api.sound.Sound;
import xyz.destiall.caramel.api.sound.SoundSource;

public final class AudioPlayer extends Component {
    private transient SoundSource source;
    private transient Sound sound;
    public String path = "";
    public boolean loop;
    public float volume;
    public boolean playOnStart;

    public AudioPlayer(GameObject gameObject) {
        super(gameObject);
    }

    @Override
    public void start() {
        rebuild();
        if (playOnStart) {
            play();
        }
    }

    @FunctionButton
    public void play() {
        if (sound != null) sound.play();
    }

    public void resume() {
        if (sound != null) sound.resume();
    }

    @FunctionButton
    public void stop() {
        if (sound != null) sound.stop();
    }

    public void pause() {
        if (sound != null) sound.pause();
    }

    public Sound getSound() {
        return sound;
    }

    public SoundSource getSource() {
        return source;
    }

    @FunctionButton
    public void rebuild() {
        if (source != null) {
            source.destroy();
        }
        source = new SoundSource(path);
        if (source.build()) {
            sound = source.createSound(loop);
            if (sound != null) {
                sound.setVolume(volume);
            }
        }
    }
}
