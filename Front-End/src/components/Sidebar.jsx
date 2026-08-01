import { NavLink } from "react-router-dom";
import { useContext, useState } from "react";
import { usercontext } from "../appcontext";
import styles from "./dashboard.module.css";

function Sidebar() {
  const { username, profilePhoto } = useContext(usercontext);
  const [isOpen, setIsOpen] = useState(false);

  const linkClass = ({ isActive }) =>
    isActive ? `${styles.navLink} ${styles.active}` : styles.navLink;

  const closeMenu = () => setIsOpen(false);

  return (
    <>
      {/* Hamburger button - only visible on mobile via CSS */}
      <button
        className={styles.hamburgerButton}
        onClick={() => setIsOpen(true)}
        aria-label="Open menu"
      >
        <svg width="22" height="22" viewBox="0 0 24 24" fill="none">
          <path d="M3 6h18M3 12h18M3 18h18" stroke="currentColor" strokeWidth="2" strokeLinecap="round" />
        </svg>
      </button>

      {/* Backdrop - only rendered/visible when drawer is open on mobile */}
      {isOpen && <div className={styles.sidebarOverlay} onClick={closeMenu} />}

      <aside className={`${styles.sidebar} ${isOpen ? styles.sidebarOpen : ""}`}>
        <div className={styles.sidebarHeader}>
          <div className={styles.avatarWrapper}>
            <img src={profilePhoto || "https://via.placeholder.com/72?text=AI"} alt="Profile" />
          </div>
          <div>
            <h2>{username || "AI Mentor"}</h2>
            <p>Career guidance dashboard</p>
          </div>
          <button className={styles.closeButton} onClick={closeMenu} aria-label="Close menu">
            ✕
          </button>
        </div>

        <nav className={styles.sidebarNav}>
          <NavLink to="/" className={linkClass} end onClick={closeMenu}>
            Overview
          </NavLink>
          <NavLink to="/ai-mentor" className={linkClass} onClick={closeMenu}>
            AI Mentor
          </NavLink>
          <NavLink to="/ai-mentor/history" className={linkClass} onClick={closeMenu}>
            Mentor History
          </NavLink>
          <NavLink to="/analysis/history" className={linkClass} onClick={closeMenu}>
            Resume History
          </NavLink>
          <NavLink to="/profile" className={linkClass} onClick={closeMenu}>
            Profile
          </NavLink>
        </nav>
      </aside>
    </>
  );
}

export default Sidebar;
