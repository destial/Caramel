package caramel.api.audio;

import caramel.api.Component;
import caramel.api.interfaces.FunctionButton;
import caramel.api.sound.SoundSource;
import caramel.api.interfaces.ShowInEditor;
import caramel.api.objects.GameObject;
import caramel.api.sound.Sound;

public final class AudioPlayer extends Component {
    private transient SoundSource source;
    private transient Sound sound;
    @ShowInEditor public String path = "";
    @ShowInEditor public boolean loop = false;
    @ShowInEditor public float volume = 0.1f;
    @ShowInEditor public boolean playOnStart = true;

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
        source = SoundSource.getSource(path);
        if (source.build()) {
            sound = source.createSound(loop);
            if (sound != null) {
                sound.setVolume(volume);
            }
        }
    }
}
