package WisdomInWords;

import java.io.Serializable;

/**
 * Created by User on 19.02.2018.
 */
public class Collocation implements Serializable {

    public boolean learnedEn;
    public String en;
    public boolean learnedRu;
    public String ru;
    public boolean isDifficult;

    public Collocation(boolean learnedEn, String en, boolean learnedRu, String ru) {
        this.learnedEn = learnedEn;
        this.en = en;
        this.learnedRu = learnedRu;
        this.ru = ru;
        this.isDifficult = false;
    }
}
