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

class Bounceable {
    static final int numcols = 10;
    static final int topCounter = 3;
    Color cols[];
    int bounce_number, counter;
    int base_color_of_red, base_color_of_green, base_color_of_blue;
    public Color scoreColor;
    Bounceable() {
		bounce_number = 0;
		counter = topCounter;
    }
    public void setColor(Graphics g) {
		g.setColor(cols[bounce_number]);
		if (bounce_number > 0 && counter-- <= 0) {
		    bounce_number--;
		    counter = topCounter;
		}
    }
    public void bounceIt() {
		bounce_number = numcols-1;
    }
    public void setColorBase(int rx, int gx, int bx) {
		base_color_of_red = rx;
		base_color_of_green = gx;
		base_color_of_blue = bx;
		int i;
		cols = new Color[numcols];
		for (i = 0; i != numcols; i++) {
		    int v = 255*i/(numcols-1);
		    cols[i] = new Color(base_color_of_red*v, base_color_of_green*v, base_color_of_blue*v);
		}
		scoreColor = cols[numcols-1];
    }
}

class Paddle extends Bounceable {
    int _x;
    int _oy;
    int targetpos;
    public int _score_x;
    int _width, dir, rangemin, rangemax, _height;
    public int score;
    public Paddle(int vp, int fp, int sp, int w) {
		_x = vp;
		_oy = fp;
		_score_x = sp;
		_width = w;
		_height = 8;
		dir = 5;
		targetpos = vp;
    }
    public Rectangle getRect() {
		return new Rectangle(_x, _oy-_height/2, _width, _height);
    }
    public void setTarget(int x) {
		targetpos = x-_width/2;
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
		if (_x < rangemin)
		    _x = rangemin;
		if (_x > rangemax)
		    _x = rangemax;
    }
    public void move() {
		int d = targetpos - _x;
		if (d < -dir)
		    d = -dir;
		if (d > dir)
		    d = dir;
		move(_x+d);
    }
    public void setRange(int mn, int mx) {
		rangemin = mn;
		rangemax = mx-_width+1;
    }
    public void draw(Graphics g) {
        setColor(g);
    	g.fillRect(_x, _oy-_height/2, _width, _height);
    }
}

class Ball extends Bounceable {
    Point _point, _start_point;
    Pong game;
    int dx, dy;
    int _size;
    int xrangemin, xrangemax, yrangemin, yrangemax;
    public boolean inPlay;
    Random random;
    public Ball(Point ps, int s, Pong g) {
		_point = ps;
		_start_point = new Point(_point.x, _point.y);
		game = g;
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
    int randBounce(int d) {
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
		
		Rectangle xrg = new Rectangle(_point.x-_size/2, _point.y-_size/2, _size, _size);
		Rectangle prg = paddle.getRect();
		xrg.translate(dx, 0);
		if (prg.intersects(xrg)) {
		    dx = randBounce(dx);
		    bounced = true;
		}
		Rectangle yrg = new Rectangle(_point.x-_size/2, _point.y-_size/2, _size, _size);
		yrg.translate(dx, dy);
		if (prg.intersects(yrg)) {
		    dy = randBounce(dy);
		    bounceIt();
		    bounced = true;
		}
		return bounced;
    }
    public void move() {
		if (!inPlay)
		    return;
		_point.x += dx;
		_point.y += dy;
		if (_point.x < xrangemin) {
		    _point.x = xrangemin;
		    dx = -dx;
		}
		if (_point.x > xrangemax) {
		    _point.x = xrangemax;
		    dx = -dx;
		}
		if (_point.y < yrangemin) {
		    inPlay = false;
		    game.updateScore(0);
		}
		if (_point.y > yrangemax) {
		    inPlay = false;
		    game.updateScore(1);
		}
    }
    public int getPaddlePos() {
		return _point.x;
    }
    public void setRange(int mnx, int mxx, int mny, int mxy) {
		xrangemin = mnx+_size/2;
		xrangemax = mxx-_size/2;
		yrangemin = mny+_size/2;
		yrangemax = mxy-_size/2;
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
    public static final int defaultPause = 100;
    int pause;
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
    public void updateScore(int which) {
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
		paddles[1].setTarget(ball.getPaddlePos());
		paddles[0].move();
		if (ball.inPlay)
		    paddles[1].move();
		if (ball.bounce(paddles[0]))
		    paddles[0].bounceIt();
		if (ball.bounce(paddles[1]))
		    paddles[1].bounceIt();
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
		    FontMetrics fm = g.getFontMetrics();
		    if (paddles[0].score == 0 && paddles[1].score == 0)
				drawBanner(g);
		    else
				for (int i = 0; i != 2; i++) {
				    String score = Integer.toString(paddles[i].score);
				    g.setColor(paddles[i].scoreColor);
				    drawCenterString(g, fm, score, paddles[i]._score_x);
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

