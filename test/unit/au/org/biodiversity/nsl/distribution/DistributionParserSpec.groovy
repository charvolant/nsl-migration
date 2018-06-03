package au.org.biodiversity.nsl.distribution

import au.org.biodiversity.nsl.distribution.DistributionParser
import spock.lang.Specification

/**
 * Unit tests for the distribution parser
 */
public class DistributionParserSpec extends Specification {
    def "test parse region"() {
        given:
        DistributionParser parser = new DistributionParser()
        when:
        List<DistributionParser.Specifier> specs = parser.parse("Qld")
        then:
        specs.size() == 1
        def spec = specs[0]
        spec.region.region == 'Qld'
        spec.region.doubtful == false
        spec.qualifiers.empty
        spec.unparse() == "Qld"
    }

    def "test parse doubtful region 1"() {
        given:
        DistributionParser parser = new DistributionParser()
        when:
        List<DistributionParser.Specifier> specs = parser.parse("Qld?")
        then:
        specs.size() == 1
        def spec = specs[0]
        spec.region.region == 'Qld'
        spec.region.doubtful == true
        spec.qualifiers.empty
        spec.unparse() == "?Qld"

    }

    def "test parse doubtful region 2"() {
        given:
        DistributionParser parser = new DistributionParser()
        when:
        List<DistributionParser.Specifier> specs = parser.parse("?Qld")
        then:
        specs.size() == 1
        def spec = specs[0]
        spec.region.region == 'Qld'
        spec.region.doubtful == true
        spec.qualifiers.empty
        spec.unparse() == "?Qld"
    }


    def "test parse long region"() {
        given:
        DistributionParser parser = new DistributionParser()
        when:
        List<DistributionParser.Specifier> specs = parser.parse("Northern")
        then:
        specs.size() == 1
        def spec = specs[0]
        spec.region.region == 'Northern'
        spec.region.doubtful == false
        spec.qualifiers.empty
        spec.unparse() == "'Northern'"
    }

    def "test parse quoted region"() {
        given:
        DistributionParser parser = new DistributionParser()
        when:
        List<DistributionParser.Specifier> specs = parser.parse("'Something (else)'")
        then:
        specs.size() == 1
        def spec = specs[0]
        spec.region.region == 'Something (else)'
        spec.region.doubtful == false
        spec.qualifiers.empty
        spec.unparse() == "'Something (else)'"
    }

    def "test parse one qualifier 1"() {
        given:
        DistributionParser parser = new DistributionParser()
        when:
        List<DistributionParser.Specifier> specs = parser.parse("Qld (native)")
        then:
        specs.size() == 1
        def spec = specs[0]
        spec.region.region == 'Qld'
        spec.region.doubtful == false
        spec.qualifiers.size() == 1
        def qual = spec.qualifiers[0]
        qual.qualifier == 'native'
        spec.unparse() == "Qld (native)"
    }

    def "test parse one qualifier 2"() {
        given:
        DistributionParser parser = new DistributionParser()
        when:
        List<DistributionParser.Specifier> specs = parser.parse("Qld (doubtful native)")
        then:
        specs.size() == 1
        def spec = specs[0]
        spec.region.region == 'Qld'
        spec.region.doubtful == false
        spec.qualifiers.size() == 1
        def qual = spec.qualifiers[0]
        qual.qualifier == 'doubtful native'
        spec.unparse() == "Qld (doubtful native)"
    }


    def "test parse two qualifiers 1"() {
        given:
        DistributionParser parser = new DistributionParser()
        when:
        List<DistributionParser.Specifier> specs = parser.parse("Qld (native, naturalised)")
        then:
        specs.size() == 1
        def spec = specs[0]
        spec.region.region == 'Qld'
        spec.region.doubtful == false
        spec.qualifiers.size() == 2
        def qual1 = spec.qualifiers[0]
        qual1.qualifier == 'native'
        def qual2 = spec.qualifiers[1]
        qual2.qualifier == 'naturalised'
        spec.unparse() == "Qld (native and naturalised)"
    }

    def "test parse two qualifiers 2"() {
        given:
        DistributionParser parser = new DistributionParser()
        when:
        List<DistributionParser.Specifier> specs = parser.parse("Qld (native and naturalised)")
        then:
        specs.size() == 1
        def spec = specs[0]
        spec.region.region == 'Qld'
        spec.region.doubtful == false
        spec.qualifiers.size() == 2
        def qual1 = spec.qualifiers[0]
        qual1.qualifier == 'native'
        def qual2 = spec.qualifiers[1]
        qual2.qualifier == 'naturalised'
        spec.unparse() == "Qld (native and naturalised)"
    }

    def "test parse three qualifiers 1"() {
        given:
        DistributionParser parser = new DistributionParser()
        when:
        List<DistributionParser.Specifier> specs = parser.parse("Qld? (native, extinct and doubtfully naturalised)")
        then:
        specs.size() == 1
        def spec = specs[0]
        spec.region.region == 'Qld'
        spec.region.doubtful == true
        spec.qualifiers.size() == 3
        def qual1 = spec.qualifiers[0]
        qual1.qualifier == 'native'
        def qual2 = spec.qualifiers[1]
        qual2.qualifier == 'extinct'
        def qual3 = spec.qualifiers[2]
        qual3.qualifier == 'doubtfully naturalised'
        spec.unparse() == "?Qld (native, extinct and doubtfully naturalised)"
    }

}
