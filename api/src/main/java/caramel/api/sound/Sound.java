package caramel.api.sound;

import static org.lwjgl.openal.AL10.AL_BUFFER;
import static org.lwjgl.openal.AL10.AL_GAIN;
import static org.lwjgl.openal.AL10.AL_LOOPING;
import static org.lwjgl.openal.AL10.AL_PLAYING;
import static org.lwjgl.openal.AL10.AL_POSITION;
import static org.lwjgl.openal.AL10.AL_SOURCE_STATE;
import static org.lwjgl.openal.AL10.alDeleteSources;
import static org.lwjgl.openal.AL10.alGenSources;
import static org.lwjgl.openal.AL10.alGetSourcef;
import static org.lwjgl.openal.AL10.alGetSourcei;
import static org.lwjgl.openal.AL10.alSourcePause;
import static org.lwjgl.openal.AL10.alSourcePlay;
import static org.lwjgl.openal.AL10.alSourceStop;
import static org.lwjgl.openal.AL10.alSourcef;
import static org.lwjgl.openal.AL10.alSourcei;

public final class Sound {
    private final SoundSource source;
    private int sourceId;
    private boolean loop = false;
    private float volume = 0f;

    protected Sound(SoundSource source) {
        this.source = source;

        sourceId = alGenSources();
        alSourcei(sourceId, AL_BUFFER, source.bufferId);
        setPosition(0);
        setLoop(false);
        setVolume(0.4f);
    }

    public void setVolume(float volume) {
        this.volume = volume;
        alSourcef(sourceId, AL_GAIN, volume);
    }

    public float getVolume() {
        return alGetSourcef(sourceId, AL_GAIN);
    }

    public boolean isLooping() {
        return loop;
    }

    public void setLoop(boolean loop) {
        this.loop = loop;
        alSourcei(sourceId, AL_LOOPING, loop ? 1 : 0);
    }

    public void setPosition(int pos) {
        alSourcei(sourceId, AL_POSITION, pos);
    }

    public int getPosition() {
        return alGetSourcei(sourceId, AL_POSITION);
    }

    public boolean isPlaying() {
        int state = alGetSourcei(sourceId, AL_SOURCE_STATE);
        return state == AL_PLAYING;
    }

    public SoundSource getSource() {
        return source;
    }

    public void play() {
        play(false);
    }

    public void play(boolean cont) {
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
        System.out.println("stopping " + this);
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
}
