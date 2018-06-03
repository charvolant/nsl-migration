package au.org.biodiversity.nsl.migration

class NormalisationController {
    def normalisationService

    def index() { }

    def normaliseDistributions() {
        def report = normalisationService.normaliseDesitributions(request.userPrincipal?.name)
        render(view: "normaliseDistribution",  model: [report: report])
    }
}
