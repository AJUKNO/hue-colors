package nl.hva.huecolors.data

/**
 * Resource used to display different states on composables
 *
 * @param T Generic class
 * @constructor Create Resource
 * @property data Holds data
 * @property message Holds a message to display
 */
sealed class Resource<T>(
    var data: T? = null, val message: String? = null
) {
    class Success<T>(data: T? = null) : Resource<T>(data)
    class Error<T>(message: String?, data: T? = null) : Resource<T>(data, message)
    class Loading<T> : Resource<T>()
    class Empty<T> : Resource<T>()
}
