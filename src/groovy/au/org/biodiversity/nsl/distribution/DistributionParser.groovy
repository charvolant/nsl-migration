package au.org.biodiversity.nsl.distribution

/**
 * Parses distribution descriptions.
 *
 * Distributions have the following form:
 *
 * <pre>
 * distribution = specifier ([',', '|']? specifier)*
 * specifier = region ('(' qualifier (',' qualifier)* (' and ' qualifier)?
 * region = '''[^''']*''' | '?'? [A-Za-z0-9]+ '?'?
 * qualifier = text
 * </pre>
 *
 * The parser is fairly loose, to allow misspellings and other messinesses
 * to be handled.
 */
class DistributionParser {
    /**
     * Parse a distribution.
     *
     * @param dist The distribution
     *
     * @return A list of distribution specifiers
     */
    List<Specifier> parse(String dist) {
        Tokens tokens = new Tokens(dist)
        List<Specifier> specifiers = new ArrayList<Specifier>()
        Specifier spec = parseSpecifier(tokens)

        while (spec != null) {
            specifiers.add(spec)
            tokens.whitespace()
            if (tokens.ch == ',' || tokens == '|')
                tokens.next()
            spec = parseSpecifier(tokens)
        }
        return specifiers
    }

    private Specifier parseSpecifier(Tokens tokens) {
        Region region = parseRegion(tokens)
        Specifier specifier
        Qualifier qualifier

        if (region == null)
            return null;
        specifier = new Specifier(region: region)
        tokens.whitespace()
        if (tokens.ch == '(') {
            tokens.next()
            qualifier = parseQualifier(tokens)
            while (qualifier != null) {
                specifier.qualifiers.add(qualifier)
                if (tokens.ch == ',') {
                    tokens.next()
                    qualifier = parseQualifier(tokens)
                } else if (tokens.ch == ')') {
                    tokens.next()
                    qualifier = null
                } else {
                    tokens.next()
                    qualifier = parseQualifier(tokens)
                }
            }
        }
        return specifier
    }

    private Region parseRegion(Tokens tokens) {
        StringBuilder b = new StringBuilder();
        boolean doubtful = false
        
        tokens.whitespace();
        if (tokens.ch == '\'') {
            tokens.next();
            while (!tokens.end && tokens.ch != '\'') {
                b.append((char) tokens.ch);
                tokens.next();
            }
            tokens.next();
        } else {
            if (tokens.ch == '?') {
                doubtful = true
                tokens.next()
                tokens.whitespace()
            }
            while(Character.isLetterOrDigit(tokens.ch)) {
                b.append((char) tokens.ch);
                tokens.next();
            }
            tokens.whitespace()
            if (tokens.ch == '?') {
                doubtful = true
                tokens.next()
            }
        }
        return b.length() == 0 ? null : new Region(region: b.toString(), doubtful: doubtful)
    }

    private static Qualifier parseQualifier(Tokens tokens) {
        final AND = ' and '
        StringBuilder b = new StringBuilder()
        int and = 0;

        tokens.whitespace();
        while(and != AND.length() && tokens.ch != ',' && tokens.ch != ')' && !tokens.end) {
            and = (AND.charAt(and) == tokens.ch) ? and + 1 : 0             
            b.append((char) tokens.ch)
            tokens.next();
        }
        if (and == AND.length()) {
            b.delete(b.length() - AND.length(), b.length())
            tokens.back()
        }
        return b.length() == 0 ? null : new Qualifier(qualifier: b.toString().trim())
    }

    private class Tokens {
        String text
        int p
        int ch

        Tokens(String text) {
            this.text = text
            this.p = 0
            this.ch = text.charAt(p)
        }

        boolean isEnd() {
            return this.p >= this.text.length()
        }

        def next() {
            this.p++
            this.ch = this.p < this.text.length() ? text.charAt(p) : -1
        }

        def back() {
            this.p--
            this.ch = this.p < this.text.length() ? text.charAt(p) : -1
        }

        def whitespace() {
            while (Character.isWhitespace(ch))
                this.next()
        }
    }

    public static class Specifier {
        Region region
        List<Qualifier> qualifiers = []
        int sortOrder = Integer.MAX_VALUE / 2
        
        String unparse() {
            StringBuilder b = new StringBuilder()

            b.append(this.region.unparse())
            if (!qualifiers.isEmpty()) {

                b.append(" (");
                for (int i = 0; i < qualifiers.size(); i++) {
                     b.append(qualifiers[i].unparse())
                    if (i == qualifiers.size() - 2)
                        b.append(" and ")
                    else if (i < qualifiers.size() - 2)
                        b.append(", ")
                }
                b.append(")")
            }
            return b.toString()
        }

    }

    public static class Region {
        String region
        boolean doubtful

        public String unparse() {
            StringBuilder b = new StringBuilder()
            boolean quote = !region.matches("\\??[A-Za-z0-9]{1,3}")
            
            if (quote)
                b.append('\'')
            if (doubtful)
                b.append('?')
            b.append(region)
            if (quote)
                b.append('\'')
            return b.toString()
        }
    }


    public static class Qualifier {
        String qualifier;

        public String unparse() {
            return this.qualifier
        }
    }
}
