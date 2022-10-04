import csstype.ClassName
import csstype.Color
import csstype.Padding
import csstype.Position.Companion.absolute
import csstype.TextAlign
import csstype.pct
import csstype.px
import emotion.react.css
import react.FC
import react.Props
import react.dom.html.ReactHTML.a
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.footer
import react.dom.html.ReactHTML.header
import react.dom.html.ReactHTML.img
import react.dom.html.ReactHTML.li
import react.dom.html.ReactHTML.main
import react.dom.html.ReactHTML.nav
import react.dom.html.ReactHTML.ul
import react.useState


val Overview = FC<Props> { _ ->
    var selectedTab: ApplicationViewState by useState(ApplicationViewState.HOME)

    header {
        nav {
            className = ClassName("navbar navbar-expand-md navbar-dark fixed-top bg-dark")
            a {
                className = ClassName("navbar-brand")
                href = "#"
                img {
                    src = "img/Ktor_Icon.png"
                    width = 50.0
                    height = 50.0
                }
            }
            div {
                className = ClassName("navbar show")
                id = "navbarNavAltMarkup"
                ul {
                    className = ClassName("navbar-nav")
                    li {
                        className = ClassName("nav-item active")
                        a {
                            className = ClassName("nav-link")
                            href = "#"
                            onClick = {
                                selectedTab = ApplicationViewState.HOME

                            }
                            +"Home"
                        }
                    }
                    li {
                        className = ClassName("nav-item")
                        a {
                            className = ClassName("nav-link")
                            href = "#"
                            onClick = {
                                selectedTab = ApplicationViewState.USER
                            }
                            +"User"
                        }
                    }
                    li {
                        className = ClassName("nav-item")
                        a {
                            className = ClassName("nav-link")
                            href = "#"
                            onClick = {
                                selectedTab = ApplicationViewState.TRANSACTION
                            }
                            +"Transaction"
                        }
                    }
                }
            }
        }
    }

    main {
        css {
            padding = Padding(80.0.px, 15.0.px, 0.0.px);
        }
        div {
            when (selectedTab) {
                ApplicationViewState.HOME -> {
                    +"Hello User"
                }

                ApplicationViewState.USER -> {
                    +"Show User"
                }

                ApplicationViewState.TRANSACTION -> {
                    +"Show Transaction"
                }
            }
        }
    }

    footer {
        css {
            position = absolute
            bottom = 0.0.px
            width = 100.0.pct
            height = 60.0.px
            lineHeight = 60.0.px
            backgroundColor = Color("#f5f5f5")
            textAlign = TextAlign.center
        }
        +"Banking Application "
    }
}