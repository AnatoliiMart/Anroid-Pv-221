package itstep.learning.android_pv_221;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameActivity extends AppCompatActivity {
    private static final String BEST_SCORE_FILE_NAME = "best_score.2048";
    private static final int N = 4;
    private final int[][] cells = new int[N][N];
    private int[][] undo;
    private int score, bestScore;
    private int prevScore; // for undo
    private final TextView[][] tvCells = new TextView[N][N];
    private TextView tvScore, tvBestScore;

    private Animation spawnAnimation, collapseAnimation, bestScoreAnimation;
    private final Random random = new Random();

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_game);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.game_layout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        LinearLayout gameField = findViewById(R.id.game_ll_field);
        tvScore = findViewById(R.id.game_tv_score);
        tvBestScore = findViewById(R.id.game_tv_best_score);
        findViewById(R.id.game_btn_new).setOnClickListener(v -> btnNewClick());
        findViewById(R.id.game_btn_undo).setOnClickListener(v -> undoMove());
        spawnAnimation = AnimationUtils.loadAnimation(this, R.anim.game_spawn);
        collapseAnimation = AnimationUtils.loadAnimation(this, R.anim.game_collapse);
        bestScoreAnimation = AnimationUtils.loadAnimation(this, R.anim.best_score_anim);
        gameField.post(() -> {
            int vw = this.getWindow().getDecorView().getWidth();
            int fieldMargin = 25;
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    vw - 2 * fieldMargin,
                    vw - 2 * fieldMargin
            );
            layoutParams.setMargins(fieldMargin, fieldMargin, fieldMargin, fieldMargin);
            layoutParams.gravity = Gravity.CENTER;
            gameField.setLayoutParams(layoutParams);
        });
        gameField.setOnTouchListener(
                new OnSwipeListener(GameActivity.this) {
                    @Override
                    public void onSwipeBottom() {
                        if (moveDown()) {
                            saveField();
                            spawnCell();
                            showField();
                        } else {
                            Toast.makeText(GameActivity.this, "No down move", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onSwipeLeft() {
                        if (canMoveLeft()) {
                            saveField();
                            moveLeft();
                            spawnCell();
                            showField();
                        } else {
                            Toast.makeText(GameActivity.this, "No left move", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onSwipeRight() {
                        if (moveRight()) {
                            saveField();
                            spawnCell();
                            showField();
                        } else {
                            Toast.makeText(GameActivity.this, "No right move", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onSwipeTop() {
                        if (moveUp()) {
                            saveField();
                            spawnCell();
                            showField();
                        } else {
                            Toast.makeText(GameActivity.this, "No up move", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        initField();
        spawnCell();
        showField();
    }

    private void saveField() {
        prevScore = score;
        undo = new int[N][N];
        for (int i = 0; i < N; i++) {
            System.arraycopy(cells[i], 0, undo[i], 0, N);
        }

    }

    private void saveBestScore() {
        try (FileOutputStream fos = openFileOutput(BEST_SCORE_FILE_NAME, Context.MODE_PRIVATE);
             DataOutputStream writer = new DataOutputStream(fos)
        ) {
            writer.writeInt(bestScore);
            writer.flush();
        } catch (IOException ex) {
            Log.e("GameActivity::saveBestScore",
                    ex.getMessage() != null
                            ? ex.getMessage()
                            : "Error writing file"
            );
        }
    }

    private void loadBestScore() {
        try (FileInputStream fis = openFileInput(BEST_SCORE_FILE_NAME);
             DataInputStream reader = new DataInputStream(fis)
        ) {
            bestScore = reader.readInt();
        } catch (IOException ex) {
            Log.e("GameActivity::loadBestScore",
                    ex.getMessage() != null
                            ? ex.getMessage()
                            : "Error reading file"
            );
        }
    }

    private boolean spawnCell() {
        List<Coordinates> freeCells = new ArrayList<>();
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                if (cells[i][j] == 0) {
                    freeCells.add(new Coordinates(i, j));
                }
            }
        }
        if (freeCells.isEmpty())
            return false;
        Coordinates randomCoordinate = freeCells.get(random.nextInt(freeCells.size()));
        cells[randomCoordinate.x][randomCoordinate.y] = random.nextInt(10) == 0 ? 4 : 2;
        tvCells[randomCoordinate.x][randomCoordinate.y].setTag(spawnAnimation);
        return true;
    }

    private void undoMove() {
        if (undo == null) {
            showUndoMessage();
            return;
        }
        for (int i = 0; i < N; i++) {
            System.arraycopy(undo[i], 0, cells[i], 0, N);
        }
        undo = null;
        score = prevScore;
        showField();
    }

    private void showUndoMessage() {
        new AlertDialog.Builder(this, androidx.appcompat.R.style.Base_ThemeOverlay_AppCompat_Dark_ActionBar)
                .setTitle("Обмеження")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setMessage("Скасування ходу неможливе")
                .setNeutralButton("Закрити", (dlg, btn) -> {
                })
                .setPositiveButton("Підписка", (dlg, btn) -> Toast.makeText(this, "Скоро буде", Toast.LENGTH_SHORT).show())
                .setNegativeButton("Вийти", (dlg, btn) -> finish())
                .setCancelable(false)
                .show();
    }

    //region Moves

    private boolean canMoveLeft() {
        for (int i = 0; i < N; i++) {
            for (int j = 1; j < N; j++) {
                if (cells[i][j] != 0 && (cells[i][j - 1] == 0 || cells[i][j - 1] == cells[i][j])) {
                    return true;
                }
            }
        }
        return false;
    }

    private void moveLeft() {
        for (int i = 0; i < N; i++) {
            int j0 = -1;
            for (int j = 0; j < N; j++) {
                if (cells[i][j] != 0) {
                    if (j0 == -1) {
                        j0 = j;
                    } else {
                        if (cells[i][j] == cells[i][j0]) {
                            cells[i][j] *= 2;
                            score += cells[i][j];
                            tvCells[i][j].setTag(collapseAnimation);
                            cells[i][j0] = 0;
                            j0 = -1;
                        } else {
                            j0 = j;
                        }
                    }
                }
            }
            j0 = -1;
            for (int j = 0; j < N; j++) {
                if (cells[i][j] == 0) {
                    if (j0 == -1) {
                        j0 = j;
                    }
                } else if (j0 != -1) {
                    cells[i][j0] = cells[i][j];
                    tvCells[i][j0].setTag(tvCells[i][j].getTag());
                    cells[i][j] = 0;
                    j0 += 1;
                    tvCells[i][j].setTag(null);

                }
            }
        }
    }

//    private boolean canMoveRight() {
//        for (int i = 0; i < N; i++) {
//            for (int j = N - 2; j >= 0; j--) {
//                if (cells[i][j]!= 0 && (cells[i][j + 1] == 0 || cells[i][j + 1] == cells[i][j])) {
//                    return true;
//                }
//            }
//        }
//        return false;
//    }

    private boolean moveRight() {
        boolean result = false;
        for (int i = 0; i < N; i++) {
            boolean wasShifted;
            do {
                wasShifted = false;
                for (int j = N - 1; j > 0; j--) {
                    if (cells[i][j - 1] != 0 && cells[i][j] == 0) {
                        cells[i][j] = cells[i][j - 1];
                        cells[i][j - 1] = 0;
                        wasShifted = result = true;
                    }
                }
            } while (wasShifted);
            for (int j = N - 1; j > 0; j--) {
                if (cells[i][j - 1] == cells[i][j] && cells[i][j] != 0) {
                    cells[i][j] *= 2;
                    score += cells[i][j];
                    tvCells[i][j].setTag(collapseAnimation);
                    for (int k = j - 1; k > 0; k--) {
                        cells[i][k] = cells[i][k - 1];
                    }
                    cells[i][0] = 0;
                    result = true;
                }
            }
        }
        return result;
    }

    private boolean moveUp() {
        boolean result = false;
        for (int j = 0; j < N; j++) {
            int j0 = -1;
            for (int i = 0; i < N; i++) {
                if (cells[i][j] != 0) {
                    if (j0 == -1) {
                        j0 = i;
                    } else {
                        if (cells[i][j] == cells[j0][j]) {
                            cells[j0][j] *= 2;
                            score += cells[j0][j];
                            tvCells[j0][j].setTag(collapseAnimation);
                            cells[i][j] = 0;
                            result = true;
                            j0 = -1;
                        } else {
                            j0 = i;
                        }
                    }
                }
            }
            j0 = -1;
            for (int i = 0; i < N; i++) {
                if (cells[i][j] == 0) {
                    if (j0 == -1) {
                        j0 = i;
                    }
                } else if (j0 != -1) {
                    cells[j0][j] = cells[i][j];
                    tvCells[j0][j].setTag(tvCells[i][j].getTag());
                    cells[i][j] = 0;
                    j0 += 1;
                    tvCells[i][j].setTag(null);
                    result = true;
                }
            }
        }
        return result;
    }

    private boolean moveDown() {
        boolean result = false;
        for (int j = 0; j < N; j++) {
            int j0 = -1;
            for (int i = N - 1; i >= 0; i--) {
                if (cells[i][j] != 0) {
                    if (j0 == -1) {
                        j0 = i;
                    } else {
                        if (cells[i][j] == cells[j0][j]) {
                            cells[j0][j] *= 2;
                            score += cells[j0][j];
                            tvCells[j0][j].setTag(collapseAnimation);
                            cells[i][j] = 0;
                            result = true;
                            j0 = -1;
                        } else {
                            j0 = i;
                        }
                    }
                }
            }
            j0 = -1;
            for (int i = N - 1; i >= 0; i--) {
                if (cells[i][j] == 0) {
                    if (j0 == -1) {
                        j0 = i;
                    }
                } else if (j0 != -1) {
                    cells[j0][j] = cells[i][j];
                    tvCells[j0][j].setTag(tvCells[i][j].getTag());
                    cells[i][j] = 0;
                    j0 -= 1;
                    tvCells[i][j].setTag(null);
                    result = true;
                }
            }
        }
        return result;
    }
    //endregion

    @SuppressLint("DiscouragedApi")
    private void initField() {
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                cells[i][j] = 0;
                //cells[i][j] = (int) Math.pow(2, i * N + j);
                tvCells[i][j] = findViewById(getResources().getIdentifier("game_cell_" + i + j, "id", getPackageName()));
            }
        }
        score = 0;
        loadBestScore();
    }

    @SuppressLint("DiscouragedApi")
    private void showField() {
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                tvCells[i][j].setText(String.valueOf(cells[i][j]));
                tvCells[i][j].getBackground().setColorFilter(
                        getResources().getColor(
                                getResources().getIdentifier(
                                        cells[i][j] <= 2048
                                                ? "game_tile_" + cells[i][j]
                                                : "game_tile_other",
                                        "color",
                                        getPackageName()
                                ),
                                getTheme()
                        ), PorterDuff.Mode.SRC_ATOP
                );
                tvCells[i][j].setTextColor(
                        getResources().getColor(
                                getResources().getIdentifier(
                                        cells[i][j] <= 2048
                                                ? "game_text_" + cells[i][j]
                                                : "game_text_other",
                                        "color",
                                        getPackageName()
                                ),
                                getTheme()
                        )
                );
                if (tvCells[i][j].getTag() instanceof Animation) {
                    tvCells[i][j].startAnimation((Animation) tvCells[i][j].getTag());
                    tvCells[i][j].setTag(null);
                }
            }
        }
        tvScore.setText(getString(R.string.game_tv_score, String.valueOf(score)));
        if (bestScore < score) {
            bestScore = score;
            saveBestScore();
            tvBestScore.startAnimation(bestScoreAnimation);
        }
        tvBestScore.setText(getString(R.string.game_tv_best, String.valueOf(bestScore)));

    }

    private void btnNewClick() {
        initField();
        spawnCell();
        showField();
    }

    private static class Coordinates {
        final int x;
        final int y;

        Coordinates(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }
}
