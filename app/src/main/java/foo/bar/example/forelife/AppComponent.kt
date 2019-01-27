package foo.bar.example.forelife

import co.early.fore.core.logging.Logger
import dagger.Component
import foo.bar.example.forelife.feature.GameModel
import javax.inject.Singleton


@Singleton
@Component(modules = arrayOf(AppModule::class))
interface AppComponent {

    //expose application scope dependencies we want accessible from anywhere
    val logger: Logger
    val gameModel: GameModel

    //submodules follow
    //operator fun plus(xxxModule: XxxModule): XxxComponent
}
