package io.jettra.flux.theme;

/**
 * Themes factory and registry initializer for JettraFlux.
 * Standardizes the 12 visual themes with dynamic White and Dark color mode support.
 */
public class Themes {

    static {
        // Register the 12 canonical themes in official catalog order
        ThemeRegistry.registerTheme("FlatTheme", FlatTheme());
        ThemeRegistry.registerTheme("Theme3D", Theme3D());
        ThemeRegistry.registerTheme("FuturisticTheme", FuturisticTheme());
        ThemeRegistry.registerTheme("AstTheme", AstTheme());
        ThemeRegistry.registerTheme("AtlantisTheme", AtlantisTheme());
        ThemeRegistry.registerTheme("OceanTheme", OceanTheme());
        ThemeRegistry.registerTheme("Matrix", Matrix());
        ThemeRegistry.registerTheme("Retro", Retro());
        ThemeRegistry.registerTheme("DarkTheme", DarkTheme());
        ThemeRegistry.registerTheme("Heroes", Heroes());
        ThemeRegistry.registerTheme("SL", SL());
        ThemeRegistry.registerTheme("Core", Core());
        ThemeRegistry.registerTheme("Police", Police());
        ThemeRegistry.registerTheme("Games", Games());
        ThemeRegistry.registerTheme("Bit", Bit());

        // Backward compatibility aliases (not shown in selection menus)
        ThemeRegistry.registerAlias("SLTheme", SLTheme());
        ThemeRegistry.registerAlias("CoreTheme", CoreTheme());
        ThemeRegistry.registerAlias("HeroesTheme", HeroesTheme());
        ThemeRegistry.registerAlias("MatrixTheme", MatrixTheme());
        ThemeRegistry.registerAlias("RetroTheme", RetroTheme());
        ThemeRegistry.registerAlias("Dark", Dark());
        ThemeRegistry.registerAlias("Ocean", OceanTheme());
        ThemeRegistry.registerAlias("Flat", FlatTheme());
        ThemeRegistry.registerAlias("3D", Theme3D());
        ThemeRegistry.registerAlias("Futuristic", FuturisticTheme());
        ThemeRegistry.registerAlias("Ast", AstTheme());
        ThemeRegistry.registerAlias("Atlantis", AtlantisTheme());
        ThemeRegistry.registerAlias("PoliceTheme", PoliceTheme());
        ThemeRegistry.registerAlias("GamesTheme", GamesTheme());
        ThemeRegistry.registerAlias("BitTheme", BitTheme());
    }

    // --- SL ---
    public static ThemeData SL() {
        return SL.create();
    }

    public static ThemeData SL(ColorMode mode) {
        return SL.create(mode);
    }

    public static ThemeData SLTheme() {
        return SL();
    }

    public static ThemeData SLTheme(ColorMode mode) {
        return SL(mode);
    }

    // --- Core ---
    public static ThemeData Core() {
        return Core.create();
    }

    public static ThemeData Core(ColorMode mode) {
        return Core.create(mode);
    }

    public static ThemeData CoreTheme() {
        return Core();
    }

    public static ThemeData CoreTheme(ColorMode mode) {
        return Core(mode);
    }

    // --- Heroes ---
    public static ThemeData Heroes() {
        return Heroes.create();
    }

    public static ThemeData Heroes(ColorMode mode) {
        return Heroes.create(mode);
    }

    public static ThemeData HeroesTheme() {
        return Heroes();
    }

    public static ThemeData HeroesTheme(ColorMode mode) {
        return Heroes(mode);
    }

    // --- FlatTheme ---
    public static ThemeData FlatTheme() {
        return FlatTheme(ColorMode.WHITE);
    }

    public static ThemeData FlatTheme(ColorMode mode) {
        return JettraTheme.FLAT_THEME.create(mode);
    }

    public static ThemeData Flat() {
        return FlatTheme();
    }

    public static ThemeData Flat(ColorMode mode) {
        return FlatTheme(mode);
    }

    // --- Theme3D ---
    public static ThemeData Theme3D() {
        return Theme3D(ColorMode.WHITE);
    }

    public static ThemeData Theme3D(ColorMode mode) {
        return JettraTheme.THEME_3D.create(mode);
    }

    // --- FuturisticTheme ---
    public static ThemeData FuturisticTheme() {
        return FuturisticTheme(ColorMode.DARK);
    }

    public static ThemeData FuturisticTheme(ColorMode mode) {
        return JettraTheme.FUTURISTIC_THEME.create(mode);
    }

    public static ThemeData Futuristic() {
        return FuturisticTheme();
    }

    public static ThemeData Futuristic(ColorMode mode) {
        return FuturisticTheme(mode);
    }

    // --- AstTheme ---
    public static ThemeData AstTheme() {
        return AstTheme(ColorMode.DARK);
    }

    public static ThemeData AstTheme(ColorMode mode) {
        return JettraTheme.AST_THEME.create(mode);
    }

    public static ThemeData Ast() {
        return AstTheme();
    }

    public static ThemeData Ast(ColorMode mode) {
        return AstTheme(mode);
    }

    // --- AtlantisTheme ---
    public static ThemeData AtlantisTheme() {
        return AtlantisTheme(ColorMode.WHITE);
    }

    public static ThemeData AtlantisTheme(ColorMode mode) {
        return JettraTheme.ATLANTIS_THEME.create(mode);
    }

    public static ThemeData Atlantis() {
        return AtlantisTheme();
    }

    public static ThemeData Atlantis(ColorMode mode) {
        return AtlantisTheme(mode);
    }

    // --- OceanTheme ---
    public static ThemeData OceanTheme() {
        return OceanTheme.create();
    }

    public static ThemeData OceanTheme(ColorMode mode) {
        return OceanTheme.create(mode);
    }

    public static ThemeData Ocean() {
        return OceanTheme();
    }

    public static ThemeData Ocean(ColorMode mode) {
        return OceanTheme(mode);
    }

    // --- Matrix ---
    public static ThemeData MatrixTheme() {
        return MatrixTheme.create();
    }

    public static ThemeData MatrixTheme(ColorMode mode) {
        return MatrixTheme.getInstance().create(mode);
    }

    public static ThemeData Matrix() {
        return MatrixTheme();
    }

    public static ThemeData Matrix(ColorMode mode) {
        return MatrixTheme(mode);
    }

    // --- Retro ---
    public static ThemeData RetroTheme() {
        return RetroTheme.create();
    }

    public static ThemeData RetroTheme(ColorMode mode) {
        return RetroTheme.getInstance().create(mode);
    }

    public static ThemeData Retro() {
        return RetroTheme();
    }

    public static ThemeData Retro(ColorMode mode) {
        return RetroTheme(mode);
    }

    // --- DarkTheme ---
    public static ThemeData DarkTheme() {
        return DarkTheme.create();
    }

    public static ThemeData DarkTheme(ColorMode mode) {
        return DarkTheme.getInstance().create(mode);
    }

    public static ThemeData Dark() {
        return DarkTheme();
    }

    public static ThemeData Dark(ColorMode mode) {
        return DarkTheme(mode);
    }

    // --- Police ---
    public static ThemeData Police() {
        return Police.create();
    }

    public static ThemeData Police(ColorMode mode) {
        return Police.create(mode);
    }

    public static ThemeData PoliceTheme() {
        return Police();
    }

    public static ThemeData PoliceTheme(ColorMode mode) {
        return Police(mode);
    }

    // --- Games ---
    public static ThemeData Games() {
        return Games.create();
    }

    public static ThemeData Games(ColorMode mode) {
        return Games.create(mode);
    }

    public static ThemeData GamesTheme() {
        return Games();
    }

    public static ThemeData GamesTheme(ColorMode mode) {
        return Games(mode);
    }

    // --- Bit ---
    public static ThemeData Bit() {
        return Bit.create();
    }

    public static ThemeData Bit(ColorMode mode) {
        return Bit.create(mode);
    }

    public static ThemeData BitTheme() {
        return Bit();
    }

    public static ThemeData BitTheme(ColorMode mode) {
        return Bit(mode);
    }
}
