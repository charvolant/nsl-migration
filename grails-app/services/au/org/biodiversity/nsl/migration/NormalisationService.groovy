/*
    Copyright 2018 Australian National Botanic Gardens

    This file is part of NSL-domain-plugin.

    Licensed under the Apache License, Version 2.0 (the "License"); you may not
    use this file except in compliance with the License. You may obtain a copy
    of the License at http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/
package au.org.biodiversity.nsl.migration

import au.org.biodiversity.nsl.Distribution
import au.org.biodiversity.nsl.TreeElement
import au.org.biodiversity.nsl.distribution.DistributionParser
import grails.transaction.Transactional

import java.text.SimpleDateFormat

@Transactional
class NormalisationService {
    static final APC_DIST = "APC Dist."
    static final DEFAULT_USER = "NSL"
    static final ISO8601 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX")

    def grailsApplication
    def messageSource

    /**
     * Normalise text contained in the 'APC Dist.' profile element
     * to reflect the specifictions in the distribution table
     */
    def normaliseDesitributions(String user) {
        def parser = new DistributionParser()
        def report = []
        // Build a map of distribution definitions
        def distLookup = Distribution.findAll().inject([:], { map, dist ->
            try {
                map[dist.description] = dist
            } catch (Exception ex) {
                report << [code: 'normalisation.distribution.error', args: [dist.description], error: ex.message]
            }
            map
        })
        Closure query = { Map params ->
            TreeElement.listOrderById(params)
        }
        chunkThis( 1000, query) { List<TreeElement> elements, bottom, top ->
            TreeElement.withSession { session ->
                elements.each { element ->
                    String link = element.sourceElementLink ?: "${grailsApplication.config.services.link.mapperURL}/links/${element.sourceShard.toLowerCase()}/treeElement/${element.id}"
                    String label = element.displayHtml
                    boolean mapped = false
                    String normalised = null
                    def dist = element.profile?.get(APC_DIST)
                    def specification = dist?.value
                    if (specification) {
                        mapped = true
                        try {
                            List<DistributionParser.Specifier> specs = parser.parse(specification)
                            def normalisedSpecs = specs.collect { spec ->
                                def ss = spec.unparse()
                                def nd = distLookup[ss]
                                if (!nd) {
                                    report << [element: label, link: link, code: 'normalisation.distribution.noMatch', args: [ss, specification]]
                                    mapped = false
                                }
                                nd ? [spec: nd.description, sortOrder: nd.sortOrder] : [spec: ss, sortOrder: Integer.MAX_VALUE / 2]
                            }
                            normalisedSpecs.sort { s1, s2 -> s1.sortOrder - s2.sortOrder }
                            normalised = normalisedSpecs.collect({ it.spec }).join(', ')
                        } catch (Exception ex) {
                            log.info("Unable to update distribution ${specification} for tree element ${element.id}: ${ex.message}")
                            report << [element: label, link: link, code: 'normalisation.distribution.error', args: [specification], error: ex.message]
                            mapped = false
                        }
                        if (mapped && !(dist.value == normalised)) {
                            dist.value = normalised
                            dist.updated_at = ISO8601.format(new Date())
                            dist.updated_by = user ?: DEFAULT_USER
                            if (!element.save()) {
                                report << [element: label, link: link, code: 'normalisation.distribution.noSave', args: [dist.toString()], error: element.errors.allErrors.toString()]
                            } else {
                                report << [element: label, link: link, code: 'normalisation.distribution.updated', args: [specification, normalised]]
                            }
                        } else
                            element.discard()
                    }
                }
                session.flush()
            }
        }
        return report
    }

    static chunkThis(Integer chunkSize, Closure query, Closure work) {

        Integer i = 0
        Integer size = chunkSize
        while (size == chunkSize) {
            Integer top = i + chunkSize
            //needs to be ordered or we might repeat items
            List items = query([offset: i, max: chunkSize])
            work(items, i, top)
            i = top
            size = items.size()
        }
    }
}
