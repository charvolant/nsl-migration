package au.org.biodiversity.nsl.migration

import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification
import au.org.biodiversity.nsl.Distribution
import au.org.biodiversity.nsl.TreeElement

import java.sql.Timestamp

/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@TestFor(NormalisationService)
@Mock([Distribution, TreeElement])
class NormalisationServiceSpec extends Specification {
    static USER1 = "XXX"
    static USER2 = "USER"
    def now

    def setup() {
        now = NormalisationService.ISO8601.format(new Date())
        grailsApplication.config.services.link.mapperURL = "http://localhost:7070"
    }

    def cleanup() {
    }

    void "test simple normalise distribution"() {
        given:
        def distribution = new Distribution(description: "Qld (native)", region: "Qld").save(failOnError: true)
        def element = new TreeElement(
                rank: 'Species',
                nameElement: 'Acacia dealbata',
                displayHtml: 'Acacia dealbata',
                instanceId: 201,
                instanceLink: 'http://localhost/instance/test/201',
                nameId: 202,
                nameLink:  'http://localhost/name/test/202',
                simpleName: 'Acacia dealbata',
                sourceShard: 'test',
                synonymsHtml: '<synonyms></synonyms>',
                updatedAt: new Timestamp(System.currentTimeMillis()),
                updatedBy: USER1,
                profile: [ "APC Dist.": ["value": "Qld   (native)", created_by: USER1, created_at: now, updated_by: USER1, updated_at: now]]
        ).save(failOnError: true)
        when:
        def report = service.normaliseDesitributions(USER2)
        then:
        def element1 = TreeElement.get(element.id)
        def distribution1 = Distribution.get(distribution.id)
        element1 != null
        element1.profile != null
        distribution1 != null
        def dist = element1.profile[service.APC_DIST]
        dist != null
        dist.value == distribution1.description
        dist.updated_by == USER2
        dist.updated_at != null
        (dist.updated_at <=> now) >= 0
        report.size() == 1
        report[0].link == "${grailsApplication.config.services.link.mapperURL}/links/test/treeElement/${element.id}"
        report[0].code == 'normalisation.distribution.updated'
    }

    void "test absent normalise distribution"() {
        given:
        def distribution = new Distribution(description: "Qld (native)", region: "Qld").save(failOnError: true)
        def element = new TreeElement(
                rank: 'Species',
                nameElement: 'Acacia dealbata',
                displayHtml: 'Acacia dealbata',
                instanceId: 201,
                instanceLink: 'http://localhost/instance/test/201',
                nameId: 202,
                nameLink:  'http://localhost/name/test/202',
                simpleName: 'Acacia dealbata',
                sourceShard: 'test',
                synonymsHtml: '<synonyms></synonyms>',
                updatedAt: new Timestamp(System.currentTimeMillis()),
                updatedBy: USER1,
                profile: [ : ]
        ).save(failOnError: true)
        when:
        def report = service.normaliseDesitributions(USER2)
        then:
        def element1 = TreeElement.get(element.id)
        def distribution1 = Distribution.get(distribution.id)
        element1 != null
        element1.profile != null
        distribution1 != null
        def dist = element1.profile[service.APC_DIST]
        dist == null
        report.size() == 0
    }

    void "test invalid normalise distribution"() {
        given:
        def distribution = new Distribution(description: "Qld (native)", region: "Qld").save(failOnError: true)
        def element = new TreeElement(
                rank: 'Species',
                nameElement: 'Acacia dealbata',
                displayHtml: 'Acacia dealbata',
                instanceId: 201,
                instanceLink: 'http://localhost/instance/test/201',
                nameId: 202,
                nameLink:  'http://localhost/name/test/202',
                simpleName: 'Acacia dealbata',
                sourceShard: 'test',
                synonymsHtml: '<synonyms></synonyms>',
                updatedAt: new Timestamp(System.currentTimeMillis()),
                updatedBy: USER1,
                profile: [ "APC Dist.": ["value": "NSW", created_by: USER1, created_at: now, updated_by: USER1, updated_at: now]]
        ).save(failOnError: true)
        def oldDist = element.profile['APC Dist.'].value
        when:
        def report = service.normaliseDesitributions(USER2)
        then:
        def element1 = TreeElement.get(element.id)
        def distribution1 = Distribution.get(distribution.id)
        element1 != null
        element1.profile != null
        distribution1 != null
        def dist = element1.profile[service.APC_DIST]
        dist != null
        dist.value == oldDist
        dist.updated_by == USER1
        dist.updated_at == now
        report.size() == 1
        report[0].link == "${grailsApplication.config.services.link.mapperURL}/links/test/treeElement/${element.id}"
        report[0].code == 'normalisation.distribution.noMatch'
    }

    void "test multiple normalise distribution"() {
        given:
        def distribution1 = new Distribution(description: "Qld (native)", region: "Qld").save(failOnError: true)
        def distribution2 = new Distribution(description: "NSW", region: "NSW").save(failOnError: true)
        def distribution3 = new Distribution(description: "Vic (native and naturalised)", region: "Vic").save(failOnError: true)
        def element = new TreeElement(
                rank: 'Species',
                nameElement: 'Acacia dealbata',
                displayHtml: 'Acacia dealbata',
                instanceId: 201,
                instanceLink: 'http://localhost/instance/test/201',
                nameId: 202,
                nameLink:  'http://localhost/name/test/202',
                simpleName: 'Acacia dealbata',
                sourceShard: 'test',
                synonymsHtml: '<synonyms></synonyms>',
                updatedAt: new Timestamp(System.currentTimeMillis()),
                updatedBy: USER1,
                profile: [ "APC Dist.": ["value": "NSW, Qld   (native), Vic (native, naturalised)", created_by: USER1, created_at: now, updated_by: USER1, updated_at: now]]
        ).save(failOnError: true)
        when:
        def report = service.normaliseDesitributions(USER2)
        then:
        def element1 = TreeElement.get(element.id)
        element1.profile != null
        def dist = element1.profile[service.APC_DIST]
        dist != null
        dist.value == distribution2.description + ", " + distribution1.description + ", " + distribution3.description
        dist.updated_by == USER2
        dist.updated_at != null
        (dist.updated_at <=> now) >= 0
        report.size() == 1
        report[0].link == "${grailsApplication.config.services.link.mapperURL}/links/test/treeElement/${element.id}"
        report[0].code == 'normalisation.distribution.updated'
    }

}
