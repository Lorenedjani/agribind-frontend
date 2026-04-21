// Standalone (non-hook) player for imperative calls outside components
let _currentPlayer: ReturnType<typeof import("expo-audio").createAudioPlayer> | null = null;

export const playAudioFromUrl = async (uri: string, rate = 1.0) => {
  const audioModule = await import("expo-audio");

  // Stop and release any currently playing audio
  if (_currentPlayer) {
    try { _currentPlayer.remove(); } catch { /* ignore */ }
    _currentPlayer = null;
  }

  // expo-audio's native AudioPlayer constructor requires 3 arguments:
  //   1. source  { uri }
  //   2. updateInterval (ms)
  //   3. shouldCorrectPitch (boolean)
  // Older versions of createAudioPlayer only forwarded 2, causing the
  // "Received 2 arguments, but 3 was expected" error.
  // Workaround: use the AudioPlayer class directly if it is exposed,
  // otherwise fall back to createAudioPlayer with a 2-arg call.
  let player: ReturnType<typeof audioModule.createAudioPlayer>;

  try {
    // expo-audio >= 0.3 exposes the AudioPlayer class directly
    const { AudioPlayer } = audioModule as any;
    if (typeof AudioPlayer === "function") {
      // new AudioPlayer(source, updateInterval, shouldCorrectPitch) — all 3 args
      player = new AudioPlayer({ uri }, 500, false);
    } else {
      // createAudioPlayer(source, options)
      player = audioModule.createAudioPlayer({ uri }, { updateInterval: 500 } as any);
    }
  } catch {
    // Last-resort: single-arg call
    player = audioModule.createAudioPlayer({ uri });
  }

  // Set playback rate if not default
  if (rate !== 1.0) {
    try { (player as any).playbackRate = rate; } catch { /* best-effort */ }
  }

  player.play();
  _currentPlayer = player;
  return player;
};

export const stopCurrentAudio = () => {
  if (_currentPlayer) {
    try { _currentPlayer.remove(); } catch { /* ignore */ }
    _currentPlayer = null;
  }
};