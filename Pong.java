// Pong.java by Paul Falstad, www.falstad.com
// Copyright (C) 1996 or something

// I had all kinds of problems getting sleep() to work for values less
// than 50 under Windows, so the frame rate isn't as good as it could be...

import java.io.InputStream;
import java.awt.*;
import java.awt.image.ImageProducer;
import java.applet.Applet;
import java.applet.AudioClip;
import java.util.Vector;
import java.util.Hashtable;
import java.util.Enumeration;
import java.io.File;
import java.net.URL;
import java.util.Random;
import java.awt.image.MemoryImageSource;

class ColorEffect {
    static final int color_number = 10;
    static final int color_counter_default = 3;
    Color cols[];
    int bounce_number, color_counter;
    int base_color_of_red, base_color_of_green, base_color_of_blue;
    public Color scoreColor;
    ColorEffect() {
		bounce_number = 0;
		color_counter = color_counter_default;
    }
    public void bounced_effect() {
		bounce_number = color_number-1;
    }
    public void setColor(Graphics g) {
		g.setColor(cols[bounce_number]);
		if (bounce_number > 0 && color_counter-- <= 0) {
		    bounce_number--;
		    color_counter = color_counter_default;
		}
    }
    public void setColorBase(int rx, int gx, int bx) {
		base_color_of_red = rx;
		base_color_of_green = gx;
		base_color_of_blue = bx;
		int i;
		cols = new Color[color_number];
		for (i = 0; i != color_number; i++) {
		    int v = 255*i/(color_number-1);
		    cols[i] = new Color(base_color_of_red*v, base_color_of_green*v, base_color_of_blue*v);
		}
		scoreColor = cols[color_number-1];
    }
}

class Paddle extends ColorEffect {
    int _x;
    int _oy;
    int _target_x;
    public int _score_y;
    int _width, _distance, _left_start, _right_end, _height;
    public int score;
    public Paddle(int vp, int fp, int sp, int w) {
		_x = vp;
		_oy = fp;
		_score_y = sp;
		_width = w;
		_height = 8;
		_distance = 5;
		_target_x = vp;
    }
    public Rectangle getRect() {
		return new Rectangle(_x, _oy-_height/2, _width, _height);
    }
    public void setTarget(int x) {
		_target_x = x-_width/2;
    }
    public int getX() {
		return _x;
    }
    public int getOY() {
		return _oy;
    }
    public int getWidth() {
		return _width;
    }
    void move(int x) {
    	_x = x;
		
		if (_x < _left_start)
		    _x = _left_start;
		
		if (_x > _right_end)
		    _x = _right_end;

    }
    public void move() {
		int d = _target_x - _x;

		if (d < -_distance)
		    d = -_distance;
		
		if (d > _distance)
		    d = _distance;
		
		move(_x+d);
    }
    public void setRange(int mn, int mx) {
		_left_start = mn;
		_right_end = mx-_width+1;
    }
    public void draw(Graphics g) {
        setColor(g);
    	g.fillRect(_x, _oy-_height/2, _width, _height);
    }
}

class Ball extends ColorEffect {
    Point _point, _start_point;
    Pong _game;
    int dx, dy;
    int _size;
    int x_left_start, x_right_end, y_top, y_bottom;
    public boolean inPlay;
    Random random;
    public Ball(Point ps, int s, Pong g) {
		_point = ps;
		_start_point = new Point(_point.x, _point.y);
		_game = g;
		_size = s;
		dx = 4;
		dy = 6;
		inPlay = false;
		random = new Random();
		setColorBase(1, 1, 0);
    }
    public void startPlay() {
		if (inPlay)
		    return;
		inPlay = true;
		dx = 4;
		dy = 6;
		_point = new Point(_start_point.x, _start_point.y);
    }
    int random_bounce(int d) {
		int dd = (d < 0) ? -1 : 1;
		int n = random.nextInt();
		if (n <= 0)
		    n = 1-n;
		return ((n % 6)+2) * -dd;
    }
    public boolean bounce(Paddle paddle) {
		int paddle_x = paddle.getX();
		int paddle_width = paddle.getWidth();

		if (_point.x < paddle_x || _point.x >= paddle_x+paddle_width)
		    return false;

		boolean bounced = false;
		
		Rectangle ball_rectangle = new Rectangle(_point.x-_size/2, _point.y-_size/2, _size, _size);
		Rectangle paddle_rectangle = paddle.getRect();
		ball_rectangle.translate(dx, 0);
		if (paddle_rectangle.intersects(ball_rectangle)) {
		    dx = random_bounce(dx);
		    bounced = true;
		}
		Rectangle yrg = new Rectangle(_point.x-_size/2, _point.y-_size/2, _size, _size);
		yrg.translate(dx, dy);
		if (paddle_rectangle.intersects(yrg)) {
		    dy = random_bounce(dy);
		    bounced_effect();
		    bounced = true;
		}
		return bounced;
    }
    public void move() {
		if (!inPlay)
		    return;
		_point.x += dx;
		_point.y += dy;
		if (_point.x < x_left_start) {
		    _point.x = x_left_start;
		    dx = -dx;
		}
		if (_point.x > x_right_end) {
		    _point.x = x_right_end;
		    dx = -dx;
		}
		if (_point.y < y_top) {
		    inPlay = false;
		    _game.addScore(0);
		}
		if (_point.y > y_bottom) {
		    inPlay = false;
		    _game.addScore(1);
		}
    }
    public int getPaddle_X() {
		return _point.x;
    }
    public void setRange(int mnx, int mxx, int mny, int mxy) {
		x_left_start = mnx+_size/2;
		x_right_end = mxx-_size/2;
		y_top = mny+_size/2;
		y_bottom = mxy-_size/2;
    }
    public void draw(Graphics g) {
		if (!inPlay)
		    return;
	        setColor(g);

		g.fillOval(_point.x-_size/2, _point.y-_size/2, _size, _size);
    }
}

public class Pong extends Applet implements Runnable {
    
    Thread thread = null;
    Paddle paddles[];
    Ball ball;
    Dimension screen;
    Font scoreFont, smallBannerFont, largeBannerFont;
    Image _image;
    int pause;
    public static final int defaultPause = 100;
    public void init() {
		
		setBackground(Color.white);

        Dimension d = screen = this.getSize();
		_image = createImage(d.width, d.height);

		paddles = new Paddle[2];
    	paddles[0] = new Paddle(10, 40, 120, 50);
    	paddles[1] = new Paddle(d.width/2, d.height-40, d.height-120, 40);
		paddles[0].setRange(0, d.width-1);
		paddles[1].setRange(0, d.width-1);
		paddles[0].setColorBase(1, 0, 0);
		paddles[1].setColorBase(0, 0, 1);
		
		ball = new Ball(new Point(d.width/2, d.height/2), 9, this);
		ball.setRange(0, d.width-1, 0, d.height-1);

		try {
			String s="PAUSE";
		    s = getParameter(s);
		    pause = (s != null) ? Integer.parseInt(s): defaultPause;
		} catch (Exception e) { 
		}

   		String font_name = "TimesRoman";
		scoreFont = new Font(font_name, Font.BOLD, 36);
		largeBannerFont = new Font(font_name, Font.BOLD, 48);
		smallBannerFont = new Font(font_name, Font.BOLD, 16);

    }
    public void addScore(int which) {
		paddles[1-which].score++;
    }
    public void run() {
		while (true) {
		    try {
				for (int i = 0; i != 3; i++)
				    step();
					repaint();
		    		Thread.currentThread().sleep(pause);
		    } catch (Exception e) {
		    
		    }
		}
    }
    public void step() {
		paddles[1].setTarget(ball.getPaddle_X());
		paddles[0].move();
		
		if (ball.inPlay)
		    paddles[1].move();

		if (ball.bounce(paddles[0]))
		    paddles[0].bounced_effect();
		
		if (ball.bounce(paddles[1]))
		    paddles[1].bounced_effect();
		
		ball.move();
    }
    public void drawCenterString(Graphics g, FontMetrics fm, String str, int ypos) {
		g.drawString(str, (screen.width-fm.stringWidth(str))/2, ypos);
    }
    public void drawBanner(Graphics g) {
		g.setFont(largeBannerFont);
		FontMetrics fm = g.getFontMetrics();
		g.setColor(Color.red);
		drawCenterString(g, fm, "PONG", 100);
		g.setColor(Color.blue);
		g.setFont(scoreFont);
		fm = g.getFontMetrics();
		drawCenterString(g, fm, "by Paul Falstad", 160);
		g.setFont(smallBannerFont);
		fm = g.getFontMetrics();
		drawCenterString(g, fm, "www.falstad.com", 190);
		g.setColor(Color.black);
		drawCenterString(g, fm, "Press mouse button to start", 300);
    }

    public void update(Graphics realg) {
		Graphics g = _image.getGraphics();
		g.setColor(getBackground());
		g.fillRect(0, 0, screen.width, screen.height);
		g.setColor(getForeground());
		if (!ball.inPlay) {
		    g.setFont(scoreFont);
		    FontMetrics fontMetrics = g.getFontMetrics();
		    if (paddles[0].score == 0 && paddles[1].score == 0)
				drawBanner(g);
		    else
				for (int i = 0; i != 2; i++) {
				    g.setColor(paddles[i].scoreColor);
				    drawCenterString(g, fontMetrics, Integer.toString(paddles[i].score), paddles[i]._score_y);
				}
		}
		for (int i = 0; i != 2; i++)
	    	    paddles[i].draw(g);
		
		ball.draw(g);
		realg.drawImage(_image, 0, 0, this);
    }

    public void start() {
		if (thread == null) {
		    thread = new Thread(this);
		    thread.start();
		}
    }

    public void stop() {
		thread = null;
    }
    public boolean handleEvent(Event evt) {
		if (evt.id == Event.MOUSE_MOVE) {
		    paddles[0].setTarget(evt.x);
		    return true;
		} else if (evt.id == Event.MOUSE_DOWN) {
		    ball.startPlay();
		    return true;
		} else {	    
		    return super.handleEvent(evt);
		}
    }
    
}

