package jokrey.utililities.swing.text_editor.text_storage;

import jokrey.utililities.swing.text_editor.FontMetricsSupplier;

import java.util.Arrays;

/**
 * Immutable.
 */
public class Line {
	private final LinePart[] parts;
	public LinePart getPart(int part_i) {
	    return parts[part_i];
    }
    public int partCount() {
	    return parts.length;
    }
    public LinePart[] getCopyOfInternalParts() {
	    return Arrays.copyOf(parts, parts.length);//Yes this does not internally copy references, but that is fine because LineParts are immutable anyways.
    }
    
    public Line(LinePart... cont) {
        parts = validate(Arrays.copyOf(cont, cont.length));
    }
    public Line(String s) {
	    this(s, null);
    }
    public Line(LinePartLayout insert_layout) {
        this("", insert_layout);
    }
	public Line(String s, LinePartLayout insert_layout) {
        parts = new LinePart[]{new LinePart(s, insert_layout)};//no need to validate. Definitly fine.
	}

	//removes empty LinePart's and merges those with the same layout. Leaves 1 empty part.
	private LinePart[] validate(LinePart[] tovalidate) {
	    LinePart[] valid_but_padded = new LinePart[tovalidate.length];

	    int vi=0;
	    for(int i=0;i<tovalidate.length; i++) {
            LinePart lp_at_i = tovalidate[i];
            LinePart lp_at_ipp;
            if(i+1 < tovalidate.length && lp_at_i.sameLayoutAs(lp_at_ipp = tovalidate[i+1])) {
                LinePart newSequence = lp_at_i.copy_change(lp_at_i.txt+lp_at_ipp.txt);
                tovalidate[i+1] = newSequence;
            } else if(!lp_at_i.isEmpty()) {
                valid_but_padded[vi] = lp_at_i;
                vi++;
            }
        }

        if(vi==0) {
            if(tovalidate.length==0)
                return new LinePart[]{new LinePart("")};
            else
                return new LinePart[]{tovalidate[0]};//tovalidate[0] must be an empty string. Otherwise vi couldn't be 0
        }
        LinePart[] valid = new LinePart[vi];
	    System.arraycopy(valid_but_padded, 0, valid, 0, valid.length);
        return valid;
	}

//String content wrapper
    public int length() {
        return toString().length();
    }
    public boolean isEmpty() {
        return toString().isEmpty();
    }
    @Override public String toString() {
	    return LinePart.toString(parts);
    }



    //querying text.
    public char getSingleCharAt(int x) {
	    return toString().charAt(x);
    }
	public LinePart getSingleCharAt_AsLinePart(int x) {
	    if(x<0 || x>=length())
	        throw new ArrayIndexOutOfBoundsException("x="+x);

		for(int ovCnt = 0, i = 0; i< partCount(); i++) {
			if(ovCnt + getPart(i).length() > x) {
				LinePart lp = getPart(i);
				return lp.copy_change(Character.toString(lp.txt.charAt(x-ovCnt)));
			} else
				ovCnt+= getPart(i).length();
		}
        throw new ArrayIndexOutOfBoundsException("Dev Error.");
	}


	public Line[] splitAt(int x) {
        if(x < 0 || x > length())
            throw new ArrayIndexOutOfBoundsException(x+", l="+length());

		for(int ovCnt = 0, i = 0; i<partCount(); i++) {
			if(ovCnt+getPart(i).length()>=x) {
				int xInSequence = x-ovCnt;
				LinePart[] split = getPart(i).splitAt(xInSequence);

				int part_i = 0;
				LinePart[] before = new LinePart[i+1];
				System.arraycopy(parts, 0, before, 0, before.length-1);
                part_i+=before.length-1;
                before[part_i++] = split[0];

                LinePart[] after = new LinePart[(partCount() - part_i) + 1];
                after[0] = split[1];
                System.arraycopy(parts, part_i, after, 1, after.length-1);

                return new Line[]{new Line(before),new Line(after)};
			} else
				ovCnt+=getPart(i).length();
		}
        throw new ArrayIndexOutOfBoundsException("Dev Error.");
	}

    public Line[] splitAt(int... splitpoints) {
        if(splitpoints.length==0) {
            return new Line[] {this};
        } else {
            int elapsedChars = 0;
            Line[] ret = new Line[splitpoints.length+1];
            Line rest = this;
            for (int i = 0; i < splitpoints.length; i++) {
                Line[] split = rest.splitAt(splitpoints[i]-elapsedChars);
                ret[i] = split[0];
                elapsedChars+=ret[i].length();
                rest = split[1];
            }
            ret[splitpoints.length] = rest;
            return ret;
        }
    }

    /**
     * @param start start index inclusive
     * @param end end exclusive
     */
    public Line getSubLine(int start, int end) {
        if(start>length() || start<0)
            throw new ArrayIndexOutOfBoundsException("start="+start);
        if(end>length() || end<0)
            throw new ArrayIndexOutOfBoundsException("end="+end);

        Line[] split = splitAt(start);
        split = split[1].splitAt(end-start);
        return split[0];
    }
    /**
     * @param start start index inclusive
     * @param end end exclusive
     */
    public String substring(int start, int end) {
        return getSubLine(start, end).toString();
    }
    /**
     *
     * @param start start index inclusive
     * @param end end exclusive
     */
	public LinePart[] getSubSequences(int start, int end) {
        return getSubLine(start, end).parts;//returning the array here is fine, because it is already a copy.
	}


	//adding text
    public Line insert(int x, String cleanStr, LinePartLayout layoutForInsert) {
	    return insert(x, new LinePart(cleanStr, layoutForInsert));
    }
    public Line insert(int x, LinePart... toinsert) {
        if(x<0 || x>length())
            throw new ArrayIndexOutOfBoundsException("x="+x);

        Line[] insert_between = splitAt(x);

        int before_length = x==0?0:insert_between[0].partCount();
        int after_length = x==length()?0:insert_between[1].partCount();
        LinePart[] inserted = new LinePart[before_length + 1 + after_length];

        int inserted_i = 0;
        System.arraycopy(insert_between[0].parts, 0, inserted, inserted_i, before_length);//before
        inserted_i+=before_length;
        System.arraycopy(toinsert, 0, inserted, inserted_i, toinsert.length);//to insert
        inserted_i+=toinsert.length;
        System.arraycopy(insert_between[1].parts, 0, inserted, inserted_i, after_length);//after

        return new Line(inserted);
    }
    public Line append(Line toappend) {
	    return insert(length(), toappend.parts);
    }


    //removing text
    public Line removeCharAt(int x) {
        return removeInterval(x, x+1);
    }
    public Line removeTextFrom(int start) {
        return removeInterval(start, length());
    }
    public Line removeTextUpTo(int end) {
        return removeInterval(0, end);
    }
    public Line removeInterval(int start, int end) {
        if(start>length() || start<0)
            throw new ArrayIndexOutOfBoundsException("start="+start+", while l="+length());
        if(end>length() || end<0)
            throw new ArrayIndexOutOfBoundsException("end="+end+", while l="+length());
        if(start==end)
            return this;

        LinePart[] before = splitAt(start)[0].parts;
        LinePart[] after = splitAt(end)[1].parts;
        int before_length = start==0?0:before.length;
        int after_length = end==length()?0:after.length;

        LinePart[] deleted = new LinePart[before_length+after_length];

        int inserted_i = 0;
        System.arraycopy(before, 0, deleted, inserted_i, before_length);//before
        inserted_i+=before_length;
        System.arraycopy(after, 0, deleted, inserted_i, after_length);//after

        return new Line(deleted);
    }





    //Pixel information for drawing..

    public int getPixelWidth()  {
        int counter = 0;
//        StringBuilder elapsedChars = new StringBuilder();
        for(int i=0;i<partCount();i++) {
            LinePart lp = getPart(i);
            counter+=lp.getPixelWidth();
//            elapsedChars.append(lp.txt);
        }
        return counter;
    }
    public int getPixelWidth(int x1, int x2)  {
        int counter = 0;
//        StringBuilder elapsedChars = new StringBuilder();
        LinePart[] subsequs = getSubSequences(x1, x2);
        for(LinePart lp:subsequs) {
            counter+=lp.getPixelWidth();
//            elapsedChars.append(lp.txt);
        }
        return counter;
    }
    public int getPixelHeight()  {
        int highest = Integer.MIN_VALUE;
        for(int i=0;i<partCount();i++) {
            LinePart lp = getPart(i);
            int lp_pixel_height = lp.getPixelHeight();
            if (highest < lp_pixel_height)
                highest = lp_pixel_height;
        }
        return highest;
    }




    //ONLY MUTABLE PART. DOESN'T CHANGE DATA(ONLY RUNTIME INFORMATION IN LINEPARTLAYOUT
    public void updatePixelKnowledge(FontMetricsSupplier display, LinePartLayout.Instantiated fallback) {
        for(int i=0;i<partCount();i++) {
            LinePart lp = getPart(i);
            lp.updateFontMetrics(display, fallback);
        }
    }

    public Line overrideLayoutWithin(int start, int end, LinePartLayout override) {
	    String content = substring(start, end);
        Line line = removeInterval(start, end);
        return line.insert(start, new LinePart(content, override));
    }
}