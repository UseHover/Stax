# detekt

## Metrics

* 14 number of properties

* 43 number of functions

* 8 number of classes

* 5 number of packages

* 8 number of kt files

## Complexity Report

* 381 lines of code (loc)

* 201 source lines of code (sloc)

* 130 logical lines of code (lloc)

* 110 comment lines of code (cloc)

* 54 cyclomatic complexity (mcc)

* 4 cognitive complexity

* 1 number of total code smells

* 54% comment source ratio

* 415 mcc per 1,000 lloc

* 7 code smells per 1,000 lloc

## Findings (1)

### style, UnnecessaryAbstractClass (1)

An abstract class is unnecessary. May be refactored to an interface or to a concrete class.

[Documentation](https://detekt.dev/docs/rules/style#unnecessaryabstractclass)

*
/Users/jumaallan/Workspace/Hover/Stax/internal/data/src/main/java/com/hover/stax/data/di/DataModule.kt:
15:16

```
An abstract class without a concrete member can be refactored to an interface.
```

```kotlin
12 
13 @Module
14 @InstallIn(SingletonComponent::class)
15 abstract class DataModule {
!!                ^ error
16 
17     @Binds
18     abstract fun bindsChannelRepository(channelRepositoryImpl: ChannelRepositoryImpl): ChannelRepository

```

generated with [detekt version 1.22.0](https://detekt.dev/) on 2023-05-02 12:58:54 UTC
