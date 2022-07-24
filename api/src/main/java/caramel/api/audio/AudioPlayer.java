package caramel.api.audio;

import caramel.api.Component;
import caramel.api.interfaces.FunctionButton;
import caramel.api.interfaces.InvokeOnEdit;
import caramel.api.interfaces.ShowInEditor;
import caramel.api.objects.GameObject;
import caramel.api.sound.Sound;
import caramel.api.sound.SoundSource;

import java.io.File;

public final class AudioPlayer extends Component {
    private transient SoundSource source;
    private transient Sound sound;
    @ShowInEditor @InvokeOnEdit("rebuild") public File file;
    @ShowInEditor @InvokeOnEdit("rebuild") public boolean loop = false;
    @ShowInEditor @InvokeOnEdit("setVolume") public float volume = 0.1f;
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

    public void setVolume() {
        if (sound != null) {
            sound.setVolume(volume);
        }
    }

    public void rebuild() {
        stop();
        if (source != null) {
            source.invalidate();
        }
        if (file == null) return;
        source = SoundSource.getSource(file.getPath());
        if (source.build()) {
            sound = source.createSound(loop);
            setVolume();
        }
    }

    @Override
    public AudioPlayer clone(GameObject gameObject, boolean copyId) {
        stop();
        return (AudioPlayer) super.clone(gameObject, copyId);
    }
}
