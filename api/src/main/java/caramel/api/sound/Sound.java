package caramel.api.sound;

import caramel.api.audio.AudioListener;
import caramel.api.components.Transform;
import caramel.api.interfaces.Copyable;

import javax.annotation.Nullable;

import static org.lwjgl.openal.AL10.AL_BUFFER;
import static org.lwjgl.openal.AL10.AL_GAIN;
import static org.lwjgl.openal.AL10.AL_LOOPING;
import static org.lwjgl.openal.AL10.AL_PLAYING;
import static org.lwjgl.openal.AL10.AL_POSITION;
import static org.lwjgl.openal.AL10.AL_RENDERER;
import static org.lwjgl.openal.AL10.AL_SOURCE_STATE;
import static org.lwjgl.openal.AL10.alDeleteSources;
import static org.lwjgl.openal.AL10.alGenSources;
import static org.lwjgl.openal.AL10.alGetSourcef;
import static org.lwjgl.openal.AL10.alGetSourcei;
import static org.lwjgl.openal.AL10.alSource3f;
import static org.lwjgl.openal.AL10.alSourcePause;
import static org.lwjgl.openal.AL10.alSourcePlay;
import static org.lwjgl.openal.AL10.alSourceStop;
import static org.lwjgl.openal.AL10.alSourcef;
import static org.lwjgl.openal.AL10.alSourcei;

public final class Sound implements Copyable<Sound> {
    private final SoundSource source;
    private int sourceId;
    private boolean loop = false;
    private float volume = 0f;

    Sound(final SoundSource source) {
        this.source = source;

        sourceId = alGenSources();
        alSourcei(sourceId, AL_BUFFER, source.bufferId);
        setPosition(0);
        setLoop(false);
        setVolume(0.1f);
    }

    public void setVolume(final float volume) {
        this.volume = volume;
        alSourcef(sourceId, AL_GAIN, volume);
    }

    public float getVolume() {
        return alGetSourcef(sourceId, AL_GAIN);
    }

    public boolean isLooping() {
        return loop;
    }

    public void setLoop(final boolean loop) {
        this.loop = loop;
        alSourcei(sourceId, AL_LOOPING, loop ? 1 : 0);
    }

    public void setPosition(final int pos) {
        alSourcei(sourceId, AL_POSITION, pos);
    }

    public int getPosition() {
        return alGetSourcei(sourceId, AL_POSITION);
    }

    public boolean isPlaying() {
        return alGetSourcei(sourceId, AL_SOURCE_STATE) == AL_PLAYING;
    }

    public SoundSource getSource() {
        return source;
    }

    public void play() {
        play(false);
    }

    public void play(final boolean cont) {
        if (!cont) setPosition(0);
        alSourcef(sourceId, AL_GAIN, volume);
        alSourcePlay(sourceId);
    }

    public void resume() {
        play(true);
    }

    public void pause() {
        alSourcef(sourceId, AL_GAIN, 0);
        alSourcePause(sourceId);
    }

    public void stop() {
        alSourcef(sourceId, AL_GAIN, 0);
        alSourcei(sourceId, AL_LOOPING, 0);
        alSourceStop(sourceId);
        alDeleteSources(sourceId);

        sourceId = alGenSources();
        alSourcei(sourceId, AL_BUFFER, source.bufferId);
        setPosition(0);
        setLoop(loop);
        setVolume(volume);
    }

    public void invalidate() {
        stop();
        alDeleteSources(sourceId);
    }

    public void setLocation(@Nullable final Transform transform) {
        alSource3f(sourceId, AL_POSITION, transform == null ? 0f : transform.position.x, transform == null ? 0f : transform.position.y, transform == null ? 0f : transform.position.z);
    }

    public void setListener(@Nullable final AudioListener listener) {
        float x = listener == null ? 0f : listener.transform.position.x + listener.offset.x;
        float y = listener == null ? 0f : listener.transform.position.y + listener.offset.y;
        float z = listener == null ? 0f : listener.transform.position.z + listener.offset.z;
        alSource3f(sourceId, AL_RENDERER, x, y, z);
    }

    @Override
    public Sound copy() {
        Sound copy = new Sound(source);
        copy.setPosition(getPosition());
        copy.setLoop(isLooping());
        copy.setVolume(getVolume());
        return copy;
    }
}
