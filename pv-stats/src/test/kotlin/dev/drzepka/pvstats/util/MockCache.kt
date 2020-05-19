package dev.drzepka.pvstats.util

import javax.cache.Cache
import javax.cache.CacheManager
import javax.cache.configuration.CacheEntryListenerConfiguration
import javax.cache.configuration.Configuration
import javax.cache.integration.CompletionListener
import javax.cache.processor.EntryProcessor
import javax.cache.processor.EntryProcessorResult

class MockCache : Cache<Any, Any> {
    override fun clear() = Unit

    override fun getName(): String = ""

    override fun getAll(keys: MutableSet<out Any>?): MutableMap<Any, Any> = mutableMapOf()

    override fun getCacheManager(): CacheManager = throw NotImplementedException()

    override fun putAll(map: MutableMap<out Any, out Any>?) = Unit

    override fun <T : Any?> unwrap(clazz: Class<T>?): T = throw NotImplementedException()

    override fun putIfAbsent(key: Any?, value: Any?): Boolean = false

    override fun removeAll(keys: MutableSet<out Any>?) = Unit

    override fun removeAll() = Unit

    override fun <C : Configuration<Any, Any>?> getConfiguration(clazz: Class<C>?): C = throw NotImplementedException()

    override fun replace(key: Any?, oldValue: Any?, newValue: Any?): Boolean = false

    override fun replace(key: Any?, value: Any?): Boolean = false

    override fun iterator(): MutableIterator<Cache.Entry<Any, Any>> = throw NotImplementedException()

    override fun get(key: Any?): Any? = null

    override fun containsKey(key: Any?): Boolean = false

    override fun close() = Unit

    override fun isClosed(): Boolean = false

    override fun <T : Any?> invoke(key: Any?, entryProcessor: EntryProcessor<Any, Any, T>?, vararg arguments: Any?): T = throw NotImplementedException()

    override fun put(key: Any?, value: Any?) = Unit

    override fun getAndRemove(key: Any?): Any? = null

    override fun remove(key: Any?): Boolean = true

    override fun remove(key: Any?, oldValue: Any?): Boolean = true

    override fun deregisterCacheEntryListener(cacheEntryListenerConfiguration: CacheEntryListenerConfiguration<Any, Any>?) = Unit

    override fun getAndReplace(key: Any?, value: Any?): Any? = null

    override fun getAndPut(key: Any?, value: Any?): Any? = null

    override fun loadAll(keys: MutableSet<out Any>?, replaceExistingValues: Boolean, completionListener: CompletionListener?) = Unit

    override fun registerCacheEntryListener(cacheEntryListenerConfiguration: CacheEntryListenerConfiguration<Any, Any>?) = Unit

    override fun <T : Any?> invokeAll(keys: MutableSet<out Any>?, entryProcessor: EntryProcessor<Any, Any, T>?, vararg arguments: Any?): MutableMap<Any, EntryProcessorResult<T>> = throw NotImplementedException()

    private class NotImplementedException : Exception("Not implemented")
}