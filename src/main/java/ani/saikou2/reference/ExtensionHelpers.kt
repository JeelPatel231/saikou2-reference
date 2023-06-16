package ani.saikou2.reference

inline fun <A:Any, T, K> Iterable<T>.associateNotNull(
    transform: (T) -> Pair<K, A?>
): Map<K, A> = this.associate(transform).filterValuesNotNull()

fun <K, A:Any> Map<K, A?>.filterValuesNotNull(): Map<K, A> {
    return this.filterValues { it != null } as Map<K, A>
}