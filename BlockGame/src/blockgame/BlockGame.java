package blockgame;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class BlockGame {
	
	static class MyFrame extends JFrame {
		//상수(constant)
		static int BALL_WIDTH = 20;
		static int BALL_HEIGHT = 20;
		static int BLOCK_ROWS = 5;
		static int BLOCK_COLUMNS = 10;
		static int BLOCK_WIDTH = 40;
		static int BLOCK_HEIGHT = 20;
		static int BLOCK_GAP = 3;
		static int BAR_WIDTH = 80;
		static int BAR_HEIGHT = 20;
		static int CANVAS_WIDTH = 400 + (BLOCK_GAP * BLOCK_COLUMNS) + BLOCK_GAP;
		static int CANVAS_HEIGHT = 600;
		
		//변수(variable)
		static MyPanel myPanel = null;
		static int score = 0;
		static Timer timer =null;
		static Block[][] blocks = new Block[BLOCK_ROWS][BLOCK_COLUMNS];
		static Bar bar = new Bar();
		static Ball ball = new Ball();
		static int barXTarget = bar.x; //TargetX값은 바의 속도를 보강하기 위함.
		static int dir = 0;			/*공의 이동방향*/
		static int ballSpeed = 5;	/*(0: Up-Right) (1: Down-Right) (2: Up-Left) (3: Down-Left)*/
		static boolean isGameFinish = false;
		
		static class Ball {
			int x = (CANVAS_WIDTH / 2) - (BALL_WIDTH / 2);
			int y = (CANVAS_HEIGHT / 2) - (BALL_HEIGHT / 2);
			int width = BALL_WIDTH;
			int height = BALL_HEIGHT;
			
			Point getCenter() {
				return new Point( x + (BALL_WIDTH/2), y + (BALL_HEIGHT/2));
			}
			Point getBottomCenter() {
				return new Point( x + (BALL_WIDTH/2), y + (BALL_HEIGHT));
			}
			Point getTopCenter() {
				return new Point( x + (BALL_WIDTH/2), y);
			}
			Point getLeftCenter() {
				return new Point( x, y + (BALL_HEIGHT/2));
			}
			Point getRightCenter() {
				return new Point( x + (BALL_WIDTH), y + (BALL_HEIGHT/2));
			}
		}	
		static class Bar {
			int x = (CANVAS_WIDTH / 2) - (BAR_WIDTH / 2);
			int y = CANVAS_HEIGHT - 100;
			int width = BAR_WIDTH;
			int height = BAR_HEIGHT;
		}	
		static class Block {
			int x = 0;
			int y = 0;
			int width = BLOCK_WIDTH;
			int height = BLOCK_HEIGHT;
			int color = 0;	//0:white 1:yellow 2:blue 3:magenta 4:red
			boolean isHidden = false;	//충돌 후에 블록이 사라지도록 해야함.(Hidden)_메모리제어보다 속성제어로 없어진듯 보이게!
		}
		
		static class MyPanel extends JPanel {//드로잉을 위한 캔버스 역할
			public MyPanel() {//생성자
				this.setSize(CANVAS_WIDTH, CANVAS_HEIGHT);
				this.setBackground(Color.black);
			}
			@Override
			public void paint(Graphics g) {
				super.paint(g);
				Graphics2D g2d = (Graphics2D)g;//x,y평면에서 그래픽을 그리기위해 스윙에서 지원하는 클래스.
											   //객체 g를 Graphics2D 타입으로 다운캐스팅 한건가?		
				drawUI( g2d );
			}
			private void drawUI(Graphics2D g2d) {
				//Block 그리기
				for(int i=0; i<BLOCK_ROWS; i++) {			//i는 세로값
					for(int j=0; j<BLOCK_COLUMNS; j++) {	//j는 가로값
						if(blocks[i][j].isHidden) {
							continue;
						}
						if(blocks[i][j].color==0) {//블록색이 흰색
							g2d.setColor(Color.white);
						}
						else if(blocks[i][j].color==1) {//블록색이 노란색
							g2d.setColor(Color.yellow);
						}
						else if(blocks[i][j].color==2) {//블록색이 파란색
							g2d.setColor(Color.blue);
						}
						else if(blocks[i][j].color==3) {//블록색이 마젠타색
							g2d.setColor(Color.magenta);
						}
						else if(blocks[i][j].color==4) {//블록색이 빨간색
							g2d.setColor(Color.red);
						}
						g2d.fillRect(blocks[i][j].x, blocks[i][j].y, 
								blocks[i][j].width, blocks[i][j].height);
					}
					
					//점수 그리기
					g2d.setColor(Color.white);
					g2d.setFont(new Font("TimesRoman", Font.BOLD, 20));
					g2d.drawString("score : " + score , CANVAS_WIDTH/2 - 30, 20);
					if(isGameFinish) {
						g2d.setColor(Color.red);
						g2d.drawString("Game Finished!", CANVAS_WIDTH/2 - 55, 50);
					}
					//공 그리기
					g2d.setColor(Color.WHITE);
					g2d.fillOval(ball.x, ball.y, BALL_WIDTH, BALL_HEIGHT);
					//바 그리기
					g2d.setColor(Color.WHITE);
					g2d.fillRect(bar.x,  bar.y,  bar.width,  bar.height);
				}	
			}
		}
		
		
		public MyFrame(String title) {
			super(title);
			this.setVisible(true);				//this는 캔버스(프레임)를 말함.
			this.setSize(CANVAS_WIDTH, CANVAS_HEIGHT);
			this.setLocation(400, 150);			//스윙창의 위치를 모니터 왼쪽맨위가 아닌, 보기 편한 위치에 띄워줌.
			this.setLayout(new BorderLayout());
			this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); //창이 잘 닫히도록
			
			initData();
			
			myPanel = new MyPanel(); //클래스를 만들어서 캔버스 역할.
			this.add("Center", myPanel);
			
			setKeyListener();
			startTimer();
			
		}
		public void initData() {
			for(int i=0; i<BLOCK_ROWS; i++) {		//i는 세로값
				for(int j=0; j<BLOCK_COLUMNS; j++) {//j는 가로값
					blocks[i][j] = new Block();		//위에서는 공간만 만들어준거고 여기서 객체를 만들어줌.
					blocks[i][j].x = (BLOCK_WIDTH * j) + (BLOCK_GAP * j);
					blocks[i][j].y = 100 + (BLOCK_HEIGHT * i) + (BLOCK_GAP * i);
					blocks[i][j].width = BLOCK_WIDTH;
					blocks[i][j].height = BLOCK_HEIGHT;
					blocks[i][j].color = 4-i;		/*아래에 있을수록 흰색이 되도록하려고 4에서 세로값 빼줌.*/
					blocks[i][j].isHidden = false; 	/*0:white 1:yellow 2:blue 3:magenta 4:red*/
				}
			}
		}
		public void setKeyListener() {
			this.addKeyListener( new KeyAdapter() {
				@Override
				public void keyPressed(KeyEvent e) {//키보드 입력하면 e라는 객체변수를 통해 들어옴.
					if(e.getKeyCode() == KeyEvent.VK_LEFT) {
						System.out.println("왼쪽 방향키가 눌렸습니다.");
						barXTarget -= 20; 		//너무 끊겨서 이동하지 않도록. 보강해줌.
						if( bar.x < barXTarget) {//계속해서 키보드를 눌렀을경우 많은 값을 갑자기 이동하게됨. 방지하기위해 써줌.
							barXTarget = bar.x;	//현재 바의 값으로 초기화해줌.
						}
					}
					else if(e.getKeyCode() == KeyEvent.VK_RIGHT) {
						System.out.println("오른쪽 방향키가 눌렸습니다.");
						barXTarget += 20;
						if( bar.x > barXTarget) {
							barXTarget = bar.x;
						}
					}
				}
			});
		}
		public void startTimer() {
			timer = new Timer(20, new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {	//타이머 이벤트 
					movement();
					checkCollision();		//벽과 바에 충돌하면 방향변경
					checkCollisionBlock();	//50개의 블록에 충돌하면 방향변경
					myPanel.repaint();		//데이터가 바뀌었으니 다시 그려주기
					
					isGameFinish();					
				}
			});
			timer.start();
		}
		public void isGameFinish() {	//반환값의 타입이 boolean인 메서드는 접두사로 is를 사용한다.(명명규칙)
			//게임 클리어
			int count = 0;
			for(int i=0; i<BLOCK_ROWS; i++) {
				for(int j=0; j<BLOCK_COLUMNS; j++) {
					Block block = blocks[i][j];
					if(block.isHidden)
						count++;}
			}
			if( count == BLOCK_ROWS * BLOCK_COLUMNS) {
				//게임종료
				//timer.stop();
				isGameFinish = true;}
		}
		public void movement() {
			//바 움직임
			if( bar.x < barXTarget) {
				bar.x += 5;}
			else if( bar.x > barXTarget) {
				bar.x -=5;}	
			
			//공 움직임
			if(dir==0) {//0: Up-Right
				ball.x += ballSpeed;
				ball.y -= ballSpeed;}
			else if(dir==1) {//1: Down-Right
				ball.x += ballSpeed;
				ball.y += ballSpeed;}
			else if(dir==2) {//2: Up-Left
				ball.x -= ballSpeed;
				ball.y -= ballSpeed;}
			else if(dir==3) {//3: Down-Left
				ball.x -= ballSpeed;
				ball.y += ballSpeed;}
		}
		public boolean duplRect(Rectangle rect1, Rectangle rect2) {
			return rect1.intersects(rect2); //두개의 사각형(원과 바)의 영역이 중복되는지 체크. 자바에서 지원하는 함수.
		}
		public void checkCollision() {
			if(dir==0) {//0: Up-Right
				//벽충돌
				if(ball.y < 0) {	//윗쪽 벽에 부딪힌 경우
					dir = 1;}
				if(ball.x > CANVAS_WIDTH-BALL_WIDTH) {	//오른쪽 벽에 부딪힌 경우
					dir = 2;}
				//바충돌X
				//오른쪽 위로 이동중에는 바에 부딪힐 일 없음.
			}
			else if(dir==1) {//1: Down-Right
				//벽충돌
				if(ball.y > CANVAS_HEIGHT-BALL_HEIGHT-BALL_HEIGHT) {	//아래쪽 벽에 부딪힌 경우_게임 리셋
					dir = 0;
					ball.x = (CANVAS_WIDTH / 2) - (BALL_WIDTH / 2);
					ball.y = (CANVAS_HEIGHT / 2) - (BALL_HEIGHT / 2);
					score = 0;}
				if(ball.x > CANVAS_WIDTH-BALL_HEIGHT) {	//오른쪽 벽에 부딪힌 경우
					dir = 3;}
				//바충돌
				if( ball.getBottomCenter().y >= bar.y ) {
					if( duplRect(new Rectangle(ball.x, ball.y, ball.width, ball.height), 
								new Rectangle(bar.x, bar.y, bar.width, bar.height)) ) {
						dir = 0;
						}
				}
			}
			else if(dir==2) {//2: Up-Left
				//벽충돌
				if( ball.y < 0) {	//위쪽 벽에 부딪힌 경우
					dir = 3;}
				if( ball.x < 0 ) {	//왼쪽 벽에 부딪힌 경우
					dir = 0;}
				//바충돌X
				//왼쪽 위로 이동중에는 바에 부딪힐 일 없음.
			}
			else if(dir==3) {//3: Down-Left
				//벽충돌
				if( ball.y > CANVAS_HEIGHT-BALL_HEIGHT-BALL_HEIGHT) {	//아래쪽 벽에 부딪힌 경우_게임 리셋
					dir = 0;
					ball.x = (CANVAS_WIDTH / 2) - (BALL_WIDTH / 2);
					ball.y = (CANVAS_HEIGHT / 2) - (BALL_HEIGHT / 2);
					score=0;}
				if( ball.x < 0) {	//왼쪽 벽에 부딪힌 경우
					dir = 1;}
				//바충돌
				if( ball.getBottomCenter().y >= bar.y ) {
					if( duplRect(new Rectangle(ball.x, ball.y, ball.width, ball.height), 
								new Rectangle(bar.x, bar.y, bar.width, bar.height)) ) {
						dir = 2;
						}
					}
			}
			}
		public void checkCollisionBlock() {
			//0: Up-Right 1: Down-Right 2: Up-Left 3: Down-Left
			for(int i=0; i<BLOCK_ROWS; i++) {
				for(int j=0; j<BLOCK_COLUMNS; j++) {
					Block block = blocks[i][j];
					if(block.isHidden == false) {
						if(dir==0) {	//0: Up-Right
							if( duplRect(new Rectangle(ball.x, ball.y, ball.width, ball.height), 
										new Rectangle(block.x, block.y, block.width, block.height)) ) {
								if( ball.x > block.x + 2 &&
									ball.getRightCenter().x <= block.x + block.width -2) {
									//블록 아래쪽에 부딪혔을때
									dir = 1;
								}else {
									//블록 왼쪽에 부딪혔을때
									dir = 2;
								}
								block.isHidden = true;				
								if(block.color == 0) {//흰색 블록과 충돌했을때 score값 증가
									score += 10;
								}else if(block.color == 1) {//노란색 블록과 충돌했을때 score값 증가
									score += 20;
								}else if(block.color == 2) {//파란색 블록과 충돌했을때 score값 증가
									score += 30;
								}else if(block.color == 3) {//마젠타색 블록과 충돌했을때 score값 증가
									score += 40;
								}else if(block.color == 4) {//빨간색 블록과 충돌했을때 score값 증가
									score += 50;
								}
							}
						}
						
						else if(dir==1) {	//1: Down-Right
							if( duplRect(new Rectangle(ball.x, ball.y, ball.width, ball.height), 
									new Rectangle(block.x, block.y, block.width, block.height)) ) {
								if( ball.x > block.x + 2 &&
									ball.getRightCenter().x <= block.x + block.width -2) {
									//블록 위쪽에 부딪혔을때
									dir = 0;
								}else {
									//블록 왼쪽에 부딪혔을때
									dir = 3;
								}
								block.isHidden = true;	
								if(block.color == 0) {//흰색 블록과 충돌했을때 score값 증가
									score += 10;
								}else if(block.color == 1) {//노란색 블록과 충돌했을때 score값 증가
									score += 20;
								}else if(block.color == 2) {//파란색 블록과 충돌했을때 score값 증가
									score += 30;
								}else if(block.color == 3) {//마젠타색 블록과 충돌했을때 score값 증가
									score += 40;
								}else if(block.color == 4) {//빨간색 블록과 충돌했을때 score값 증가
									score += 50;
								}
								}
						}
						else if(dir==2) {	//2: Up-Left
							if( duplRect(new Rectangle(ball.x, ball.y, ball.width, ball.height), 
									new Rectangle(block.x, block.y, block.width, block.height)) ) {
								if( ball.x > block.x + 2 &&
									ball.getRightCenter().x <= block.x + block.width -2) {
									//블록 아래쪽에 부딪혔을때
									dir = 3;
								}else {
									//블록 오른쪽에 부딪혔을때
									dir = 0;
								}
								block.isHidden = true;		
								if(block.color == 0) {//흰색 블록과 충돌했을때 score값 증가
									score += 10;
								}else if(block.color == 1) {//노란색 블록과 충돌했을때 score값 증가
									score += 20;
								}else if(block.color == 2) {//파란색 블록과 충돌했을때 score값 증가
									score += 30;
								}else if(block.color == 3) {//마젠타색 블록과 충돌했을때 score값 증가
									score += 40;
								}else if(block.color == 4) {//빨간색 블록과 충돌했을때 score값 증가
									score += 50;
								}
								}
						}
						else if(dir==3) {	//3: Down-Left
							if( duplRect(new Rectangle(ball.x, ball.y, ball.width, ball.height), 
									new Rectangle(block.x, block.y, block.width, block.height)) ) {
								if( ball.x > block.x + 2 &&
									ball.getRightCenter().x <= block.x + block.width -2) {
									//블록 위쪽에 부딪혔을때
									dir = 2;
								}else {
									//블록 오른쪽에 부딪혔을때
									dir = 1;
								}
								block.isHidden = true;	
								if(block.color == 0) {//흰색 블록과 충돌했을때 score값 증가
									score += 10;
								}else if(block.color == 1) {//노란색 블록과 충돌했을때 score값 증가
									score += 20;
								}else if(block.color == 2) {//파란색 블록과 충돌했을때 score값 증가
									score += 30;
								}else if(block.color == 3) {//마젠타색 블록과 충돌했을때 score값 증가
									score += 40;
								}else if(block.color == 4) {//빨간색 블록과 충돌했을때 score값 증가
									score += 50;
								}
								}
						}
					}
				}
			}
		}
	}

	public static void main(String[] args) {
		
		new MyFrame("Block Game");	//생성자만 실행시켜줘도 실행됨.

	}

}
