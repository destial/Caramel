package caramel.api.audio;

import caramel.api.Component;
import caramel.api.interfaces.FileExtensions;
import caramel.api.interfaces.FunctionButton;
import caramel.api.interfaces.InvokeOnEdit;
import caramel.api.interfaces.ShowInEditor;
import caramel.api.objects.GameObject;
import caramel.api.sound.Sound;
import caramel.api.sound.SoundSource;

import java.io.File;

/**
 * This {@link Component} is used to play audio files.
 */
public final class AudioPlayer extends Component {
    private transient SoundSource source;
    private transient Sound sound;
    @ShowInEditor public AudioListener listener;
    @ShowInEditor @FileExtensions({".ogg", ".mp3"}) @InvokeOnEdit("rebuild") public File file;
    @ShowInEditor @InvokeOnEdit("setLoop") public boolean loop = false;
    @ShowInEditor @InvokeOnEdit("setVolume") public float volume = 0.1f;
    @ShowInEditor public boolean playOnStart = true;
    @ShowInEditor public boolean updatePosition = true;

    public AudioPlayer(final GameObject gameObject) {
        super(gameObject);
    }

    @Override
    public void start() {
        rebuild();
        if (playOnStart) {
            play();
        }
    }

    @Override
    public void lateUpdate() {
        if (updatePosition && sound != null && listener != null) {
            sound.setLocation(transform);
            sound.setListener(listener);
        } else if (sound != null) {
            sound.setLocation(null);
            sound.setListener(null);
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

    public void setLoop() {
        if (sound != null) {
            sound.setLoop(loop);
        }
    }

    public void rebuild() {
        stop();
        if (file == null) return;
        if (source != null) {
            source.invalidate(sound);
        }
        source = SoundSource.getSource(file.getPath());
        if (source.build()) {
            sound = source.createSound();
            setVolume();
        }
    }

    @Override
    public AudioPlayer clone(final GameObject gameObject, final boolean copyId) {
        stop();
        return (AudioPlayer) super.clone(gameObject, copyId);
    }

    public void invalidate() {
        if (sound != null) {
            sound.invalidate();
        }
    }
}
