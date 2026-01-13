# Developer Guide – Adding a New Menu Item in the Sidebar

This guide explains how a new developer can:

1. Add a new entry in the **navigation drawer** (left sidebar)
2. Implement the corresponding **screen**
3. Wire everything together so it works end-to-end

The drawer is driven by a sealed class `DrawerDestination` and is used in:

- `core/navigation/DrawerDestination.kt`
- `components/JetchatDrawer.kt`
- `NavActivity.kt`

We’ll walk through an example: adding a new screen called **“Reports”**.

---

## 1. Add a label in `strings.xml`

File: `app/src/main/res/values/strings.xml`

Add a new string resource for the menu label:

```xml
<string name="menu_reports">Reports</string>
