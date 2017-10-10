package com.github.deividasp.kchip8.extension

import java.nio.file.Paths
import kotlin.reflect.KClass

fun KClass<*>.getResourcePath(name: String) =
        Paths.get(this.java.classLoader.getResource(name)?.toURI()).toString()