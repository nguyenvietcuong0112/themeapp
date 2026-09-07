const APP_ROOT = "/Users/cuong/Documents/pheej_studio/themeapp";

const config = {
  appRoot: APP_ROOT,
  android: {
    appPath: `${APP_ROOT}/app/build/outputs/apk/debug/app-debug.apk`,
    applicationId: "com.themes.diy.widgets.keyboard.controlcenter",
  },
  devices: ["pixel-10-pro"],
  locales: ["en-US"],
  appearance: "light",
  frame: { variant: "17-pro-orange" },
  theme: {
    background: "linear-gradient(150deg, #FF5E7E 0%, #FF9966 60%, #FFA07A 100%)",
    headlineColor: "#FFFFFF",
    fontFamily: 'Montserrat, system-ui, sans-serif',
    copyHeightRatio: 0.18, // Thu gọn chiều cao phần chữ để nhường chỗ tối đa cho điện thoại
    deviceWidthRatio: 0.88,
    template: "none",
    layout: "hero",
  },
  store: {
    name: "Themes: Control Center, Icons",
    subtitle: { "en-US": "Cute Themes, DIY Widgets & Icons" },
    developer: "Pheej Studio",
    category: "Personalization",
    rating: 4.8,
    ratingCount: "3.5K Ratings",
    ageRating: "3+",
    price: "Free",
    description: {
      "en-US": "Personalize your Android device with cute aesthetic themes, dynamic control center, custom icons, and handy widgets. Express your unique style effortlessly!",
    },
  },
  scenes: [
    {
      kind: "screenshot",
      id: "themes",
      flow: "store-01-themes",
      headline: { "en-US": "Aesthetic Themes & Styles" },
      layout: "duo-tilt",
      secondScene: "icons",
    },
    {
      kind: "screenshot",
      id: "control-center",
      flow: "store-02-control-center",
      headline: { "en-US": "Custom Control Center" },
      layout: "tilt",
    },
    {
      kind: "screenshot",
      id: "icons",
      flow: "store-03-icons",
      headline: { "en-US": "Unique App Icon Packs" },
      layout: "hero",
    },
    {
      kind: "screenshot",
      id: "widgets",
      flow: "store-04-widgets",
      headline: { "en-US": "Handy DIY Widgets" },
      layout: "tilt-right",
    },
  ],
};

export default config;
