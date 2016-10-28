apitrace = job("Apitrace_Linux_amd64");

apitrace.with {
    steps {
        cmake {
            generator("Unix Makefiles")
            args("-DCMAKE_INSTALL_PREFIX=./out/")
            sourceDir("./")
            buildDir("./build/")

            buildToolStep {
                useCmake(true)
                args("--target install")
            }
        }
    }
    publishers {
        archiveArtifacts {
            pattern("./out/**")
        }
    }
}
