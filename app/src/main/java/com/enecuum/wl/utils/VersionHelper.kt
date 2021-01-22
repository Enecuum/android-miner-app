package com.enecuum.wl.utils

/**
 * Tiny helper based on https://github.com/swiftzer/semver sources with MIT License
 *
 * !!!ALLOW LEADING ZEROES!!!
 *
 * @property major major version, increment it when you make incompatible API changes.
 * @property minor minor version, increment it when you add functionality in a backwards-compatible manner.
 * @property patch patch version, increment it when you make backwards-compatible bug fixes.
 */
data class SemanticVersion(
    val major: Int = 0,
    val minor: Int = 0,
    val patch: Int = 0
) : Comparable<SemanticVersion> {
    companion object {
        /**
         * Parse the version string to [SemanticVersion] data object.
         * @param version version string.
         * @throws IllegalArgumentException if the version is not valid.
         */
        @JvmStatic
        fun parse(version: String): SemanticVersion {
            val pattern =
                Regex("""([0-9]\d*)?(?:\.)?([0-9]\d*)?(?:\.)?([0-9]\d*)?""")
            val result =
                pattern.matchEntire(version)
                    ?: throw IllegalArgumentException("Invalid version string [$version]")
            return SemanticVersion(
                major = if (result.groupValues[1].isEmpty()) 0 else result.groupValues[1].toInt(),
                minor = if (result.groupValues[2].isEmpty()) 0 else result.groupValues[2].toInt(),
                patch = if (result.groupValues[3].isEmpty()) 0 else result.groupValues[3].toInt()
            )
        }
    }

    init {
        require(major >= 0) { "Major version must be a positive number" }
        require(minor >= 0) { "Minor version must be a positive number" }
        require(patch >= 0) { "Patch version must be a positive number" }
    }

    /**
     * Build the version name string.
     * @return version name string in Semantic Versioning 2.0.0 specification.
     */
    override fun toString(): String = buildString {
        append("$major.$minor.$patch")
    }

    /**
     * Compare two SemanticVersion objects using major, minor, patch version as specified in SemanticVersion specification.
     *
     * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than the specified object.
     */
    override fun compareTo(other: SemanticVersion): Int {
        return when {
            major > other.major -> 1
            major < other.major -> -1
            minor > other.minor -> 1
            minor < other.minor -> -1
            patch > other.patch -> 1
            patch < other.patch -> -1
            else -> 0
        }
    }
}