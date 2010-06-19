package gategui;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D.Float;

/**
 *
 */
public class Gate {

    int inL = -1; // from gateNr
    char inLc = '-'; // 'L' or 'R'
    int inR = -1;
    char inRc = '-'; // 'L' or 'R'
    int outL = -1;
    char outLc = '-'; // 'L' or 'R'
    int outR = -1;
    char outRc = '-'; // 'L' or 'R'
    Float rect = null;

    public Gate() {
    }

    public Point2D.Float getTopLeft() {
        return new Point2D.Float(rect.x, rect.y);
    }

    public Point2D.Float getTopRight() {
        return new Point2D.Float(rect.x + rect.width, rect.y);
    }

    public Point2D.Float getBottomLeft() {
        return new Point2D.Float(rect.x, rect.y + rect.height);
    }

    public Point2D.Float getBottomRight() {
        return new Point2D.Float(rect.x + rect.width, rect.y + rect.height);
    }
}
