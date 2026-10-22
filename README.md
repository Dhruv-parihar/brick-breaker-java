# Brick Breaker (Java)

A classic brick breaker arcade game built with Java Swing. Control the paddle, bounce the ball into the bricks, and clear every level without losing all your lives.

![Gameplay screenshot](screenshot.png)

## Features

- Angle-based paddle bounces: where the ball hits the paddle decides where it goes
- 6 x 10 grid of colored bricks
- Score, lives (3), and increasing levels
- Ball speeds up each level, and from level 2 the top rows need two hits
- Smooth 60 FPS game loop using a Swing `Timer`
- Single file, no external libraries

## Requirements

- JDK 17 or newer

## How to Run

```bash
git clone https://github.com/Dhruv-parihar/brick-breaker-java.git
cd brick-breaker-java
javac BrickBreaker.java
java BrickBreaker
```

## Controls

| Key | Action |
| --- | --- |
| Left / Right arrows or A / D | Move paddle |
| Space | Launch ball / start next level |
| R | Restart game |

## How It Works

- **Game loop:** a `javax.swing.Timer` fires about every 16 ms, updating the game state and repainting the panel.
- **Collisions:** the ball and bricks use `Rectangle2D`. For brick hits, the smaller overlap axis decides whether the ball bounces horizontally or vertically.
- **Paddle physics:** the hit position along the paddle is mapped to an angle of up to 60 degrees, keeping the ball speed constant.
- **Game states:** `READY`, `PLAYING`, `GAME_OVER`, and `LEVEL_CLEAR`.

## Possible Improvements

- Power-ups (wide paddle, multi-ball, slow ball)
- Sound effects
- High score saving
- Mouse control

## Author

Dhruv Parihar - [GitHub](https://github.com/Dhruv-parihar)
