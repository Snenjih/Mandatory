# Contributing to MandatoryMod

Thank you for your interest in contributing. This document explains how to get set up, what the conventions are, and how to submit changes.

---

## Prerequisites

- **JDK 21** (exact version — not 17, not 22)
- **Git**
- A Minecraft account (needed for `runClient`)

---

## Setting Up

```bash
git clone https://github.com/Snenjih/Mandatory.git
cd Mandatory
./gradlew build         # first-time setup downloads dependencies
./gradlew runClient     # launch Minecraft with the mod loaded
```

---

## Project Structure

```
src/main/java/de/snenjih/mandatory/
  modules/
    api/          ← BaseModule, Module interface, setting types
    impl/         ← one subdirectory per feature module
  mixin/          ← Mixin classes
  config/         ← ModConfig (JSON persistence)
  registry/       ← ModuleRegistry, HudRegistry
  screen/         ← CarouselScreen, CarouselRenderer
  MandatoryMod.java   ← entry point
src/main/resources/
  assets/mandatory/
    textures/gui/sprites/modules/   ← 32×32 PNG icons
    lang/en_us.json                 ← translation keys
  fabric.mod.json
  mandatory.mixins.json
```

---

## Adding a New Module

1. **Create the class** in `modules/impl/<module_id>/`:
   ```java
   public class MyFeatureModule extends BaseModule {
       public MyFeatureModule() {
           super("my_feature", "My Feature", "Short description.",
                 ModuleCategory.UTILITY,
                 Identifier.of("mandatory", "modules/my_feature"));
       }
       @Override public void onEnable()  { /* subscribe to events */ }
       @Override public void onDisable() { /* unsubscribe */ }
   }
   ```

2. **Register** in `MandatoryMod.onInitializeClient()`:
   ```java
   registry.register(new MyFeatureModule());
   ```

3. **Add a 32×32 PNG icon** at:
   ```
   src/main/resources/assets/mandatory/textures/gui/sprites/modules/my_feature.png
   ```

4. **Add translation keys** to `assets/mandatory/lang/en_us.json` if needed.

5. **Write a Mixin** if the feature needs to intercept Minecraft internals, and register it in `mandatory.mixins.json`.

### HUD modules

If the module renders a movable overlay, also implement `HudElement` and register it with:
```java
HudRegistry.register(myModule, defaultX, defaultY);
```

See the existing HUD modules (e.g. `CoordinatesHudModule`) as reference.

---

## Code Conventions

- **Java 21**, no preview features.
- No comments unless the *why* is non-obvious. Well-named identifiers are preferred over inline explanations.
- No permanent event listeners in constructors — subscribe in `onEnable()`, unsubscribe in `onDisable()`.
- Do not call `onEnable()` or `onDisable()` manually; `BaseModule.setEnabled()` handles them.
- Avoid accessing `MinecraftClient.getInstance()` from constructors — use it at call time.
- Follow the 1.21.11 API specifics documented in `CLAUDE.md` (breaking API changes from older MC versions).

---

## Submitting Changes

1. Fork the repository and create a branch from `main`:
   ```bash
   git checkout -b feat/my-feature
   ```

2. Make your changes and verify the build is clean:
   ```bash
   ./gradlew compileJava
   ```

3. Commit with a conventional message:
   ```
   feat: add MyFeature module
   fix: resolve NPE in StackRefill when inventory is empty
   refactor: extract carousel scroll logic to helper
   ```

4. Open a pull request against `main`. Describe what the change does and why.

---

## Pull Request Checklist

- [ ] `./gradlew compileJava` passes with zero errors
- [ ] New module has a 32×32 PNG icon
- [ ] New module is registered in `MandatoryMod.onInitializeClient()`
- [ ] New Mixins (if any) are registered in `mandatory.mixins.json`
- [ ] Translation keys added to `en_us.json`
- [ ] No permanent event listeners outside of `onEnable()`/`onDisable()`

---

## Bug Reports & Feature Requests

Open an [issue](https://github.com/Snenjih/Mandatory/issues) with as much detail as possible:

- Minecraft version, Fabric Loader version, Fabric API version
- Steps to reproduce (for bugs)
- What you expected vs. what happened
- Crash report or log excerpt if applicable (`.minecraft/logs/latest.log`)

---

## License

By contributing, you agree that your contributions will be licensed under the [MIT License](LICENSE).
