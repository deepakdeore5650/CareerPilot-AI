import Styles from "./home.module.css";
import { useContext, useEffect, useState } from "react";
import { usercontext } from "../appcontext";
import { useNavigate } from "react-router-dom";
import { toast } from "react-toastify";
import { motion, AnimatePresence } from "framer-motion";

function Home() {
    const navigate = useNavigate();
    const { islogged, username, isprevious, serviceURL, setusername, setislogged, setisprevious } = useContext(usercontext);
    const [isshow, setshow] = useState(false);
    const [isloading, setisloading] = useState(false);
    const [mentorLoading, setMentorLoading] = useState(false);
    const [showLoginPrompt, setShowLoginPrompt] = useState(false);
    const [suggestions, setSuggestions] = useState({
        careerSuggestions: [],
        projectIdeas: [],
        skillsToLearn: [],
        improvementAreas: [],
        recommendedTechnologies: [],
        interviewPrep: [],
        missingSkills: [],
        resumeTips: [],
        personalizedRoadmap: "",
    });
    const [history, setHistory] = useState([]);
    const [mentorLatest, setMentorLatest] = useState(null);
    const [profileCompletion, setProfileCompletion] = useState(0);
    const [completionMessage, setCompletionMessage] = useState("");
    const [showDeleteModal, setShowDeleteModal] = useState(false);
    const [delloading, setdelloading] = useState(false);

    useEffect(() => {
        const closeMenu = (event) => {
            if (event.target.id !== "menu") {
                setshow(false);
            }
        };
        window.addEventListener("click", closeMenu);
        return () => window.removeEventListener("click", closeMenu);
    }, []);

    useEffect(() => {
        if (!islogged) {
            setHistory([]);
            return;
        }

        const fetchProfileAndHistory = async () => {
            try {
                const profileRes = await fetch(`${serviceURL}/profile`, {
                    method: "GET",
                    credentials: "include",
                });
                if (profileRes.ok) {
                    const data = await profileRes.json();
                    setProfileCompletion(data.profileCompletion || 0);
                    if (data.profileCompletion < 100) {
                        setCompletionMessage("Complete your profile to get better AI suggestions.");
                    } else {
                        setCompletionMessage("");
                    }
                }
            } catch (error) {
                console.error("Profile fetch failed", error);
            }

            try {
                const historyRes = await fetch(`${serviceURL}/analysis/history`, {
                    method: "GET",
                    credentials: "include",
                });
                if (historyRes.ok) {
                    setHistory(await historyRes.json());
                }
            } catch (error) {
                console.error("History fetch failed", error);
            }
            try {
                const mentorRes = await fetch(`${serviceURL}/career/history`, {
                    method: "GET",
                    credentials: "include",
                });
                if (mentorRes.ok) {
                    const m = await mentorRes.json();
                    if (m && m.length) setMentorLatest(m[0]);
                }
            } catch (error) {
                console.error("Mentor history fetch failed", error);
            }
        };

        fetchProfileAndHistory();
    }, [islogged, serviceURL]);

    const logout = () => {
        setisloading(true);
        fetch(`${serviceURL}/logout`, { method: "post", credentials: 'include' })
            .then(response => {
                if (response.ok) {
                    setusername("");
                    setislogged(false);
                    setisprevious(false);
                    toast.success("Successfully Logged out");
                    navigate("/login");
                } else {
                    toast.error("Unauthorized access");
                }
            })
            .catch(() => toast.error("Logout failed"))
            .finally(() => setisloading(false));
    };

    const toggle = (e) => {
        e.stopPropagation();
        setshow(!isshow);
    };

    const delaccount = () => {
        setdelloading(true);
        fetch(`${serviceURL}/deleteAccount`, { method: "post", credentials: 'include' })
            .then(response => {
                if (response.ok) {
                    setislogged(false);
                    setShowDeleteModal(false);
                    setusername("");
                    setisprevious(false);
                    navigate("/login");
                    toast.success("Account Deleted Successfully");
                } else {
                    toast.error("Couldn't delete, try again!");
                }
            })
            .catch(() => toast.error("Network Error"))
            .finally(() => setdelloading(false));
    };

    const upnavigate = () => {
        if (!islogged) {
            setShowLoginPrompt(true);
            toast.info("Login to analyze your resume and get personalized guidance.");
            return;
        }
        navigate("/uploaddoc");
    };

    const generateMentor = async () => {
        if (!islogged) {
            setShowLoginPrompt(true);
            toast.info("Sign in to unlock your AI mentor.");
            return;
        }
        setMentorLoading(true);
        try {
            const response = await fetch(`${serviceURL}/career/suggestions`, {
                method: "GET",
                credentials: "include",
            });
            if (!response.ok) {
                throw new Error("Unable to generate suggestions");
            }
            const data = await response.json();
            setSuggestions({
                careerSuggestions: data.careerSuggestions || [],
                projectIdeas: data.projectIdeas || [],
                skillsToLearn: data.skillsToLearn || [],
                improvementAreas: data.improvementAreas || [],
                recommendedTechnologies: data.recommendedTechnologies || [],
                interviewPrep: data.interviewPrep || [],
                missingSkills: data.missingSkills || [],
                resumeTips: data.resumeTips || [],
                personalizedRoadmap: data.personalizedRoadmap || "",
            });
            toast.success("AI Mentor suggestions ready.");
        } catch (error) {
            console.error(error);
            toast.error("Could not generate AI suggestions.");
        } finally {
            setMentorLoading(false);
        }
    };

    const closeLoginPrompt = () => {
        setShowLoginPrompt(false);
    };

    const renderCardList = (title, items) => {
        const partial = items?.slice(0, 3) || [];
        return (
            <div className={Styles.suggestionCard}>
                <div className={Styles.cardHeading}>
                    <h3>{title}</h3>
                    <button onClick={() => navigate('/ai-mentor')} className={Styles.cardButton}>View Full Details</button>
                </div>
                {partial.length ? (
                    <ul>
                        {partial.map((item, index) => (
                            <li key={`${title}-${index}`}>{item}</li>
                        ))}
                    </ul>
                ) : (
                    <p className={Styles.cardEmpty}>Ask the AI Mentor to generate insights.</p>
                )}
            </div>
        );
    };

    const formatDateTime = (value) => {
        if (!value) return "-";
        return new Date(value).toLocaleString();
    };

    return (
        <div className={Styles.container}>
            <div className={Styles.gridOverlay} />
            <div className={Styles.glowPoint} />

            <div className={Styles.gridOverlay} />
<div className={Styles.glowPoint} />
<div className={Styles.bubbles} aria-hidden="true">
    {Array.from({ length: 18 }).map((_, i) => {
        const size = 10 + (i % 6) * 7;
        return (
            <span
                key={i}
                className={Styles.bubble}
                style={{
                    left: `${(i * 53) % 100}%`,
                    width: `${size}px`,
                    height: `${size}px`,
                    animationDuration: `${13 + (i % 7) * 2.5}s`,
                    animationDelay: `${-(i % 9) * 2.4}s`,
                }}
            />
        );
    })}
</div>

            <header className={Styles.nav}>
                <div className={Styles.navContainer}>
                    <h1>CareerPilot AI</h1>
                    <div className={Styles.userWrapper}>
                        {!islogged ? (
                            <motion.button
                                whileHover={{ y: -1 }}
                                whileTap={{ scale: 0.98 }}
                                onClick={() => navigate('/login')}
                            >
                                Login
                            </motion.button>
                        ) : (
                            <motion.h3
                                id="menu"
                                onClick={toggle}
                                className={Styles.profile}
                                whileHover={{ scale: 1.05 }}
                                whileTap={{ scale: 0.95 }}
                            >
                                {username?.charAt(0)?.toUpperCase()}
                            </motion.h3>
                        )}

                        <AnimatePresence>
                            {isshow && islogged && (
                                <motion.div
                                    id="menu"
                                    className={Styles.profilemenu}
                                    initial={{ opacity: 0, y: 12, scale: 0.95 }}
                                    animate={{ opacity: 1, y: 0, scale: 1 }}
                                    exit={{ opacity: 0, y: 8, scale: 0.95 }}
                                    transition={{ duration: 0.15 }}
                                >
                                    <h2 id="menu">Signed in as <span id="menu">{username}</span></h2>
                                    <hr id="menu" />
                                    <div id="menu" className={Styles.pmenusec}>
                                        <button id="menu" onClick={() => { navigate('/profile'); setshow(false); }} disabled={isloading}>
                                            <span style={{ marginRight: 8 }}>👤</span>Profile
                                        </button>
                                        <button id="menu" onClick={() => { navigate('/analysis/history'); setshow(false); }} disabled={isloading}>
                                            <span style={{ marginRight: 8 }}>📄</span>Resume History
                                        </button>
                                        <button id="menu" onClick={() => { navigate('/ai-mentor/history'); setshow(false); }} disabled={isloading}>
                                            <span style={{ marginRight: 8 }}>🤖</span>Mentor History
                                        </button>
                                        <button id="menu" onClick={logout} disabled={isloading}>
                                            <span style={{ marginRight: 8 }}>🔒</span>Logout
                                        </button>
                                        <button id="menu" onClick={() => setShowDeleteModal(true)} disabled={isloading}>
                                            <span className={Styles.del}>Delete account</span>
                                        </button>
                                    </div>
                                </motion.div>
                            )}
                        </AnimatePresence>
                    </div>
                </div>
            </header>

            <main className={Styles.pageGrid}>
                <section className={Styles.heroSection}>
                    <motion.div
                        className={Styles.heroPanel}
                        initial={{ opacity: 0, x: -24 }}
                        animate={{ opacity: 1, x: 0 }}
                        transition={{ duration: 0.5 }}
                    >
                        <div className={Styles.badgeTag}>AI Career Mentor</div>
                        <h1>Grow your career with intelligent guidance.</h1>
                        <p>
                            Generate personalized development plans, project recommendations, missing skill insights, portfolio improvements and interview prep using Gemini AI.
                        </p>
                        <div className={Styles.btncontainer}>
                            <motion.button
                                whileHover={{ y: -2 }}
                                whileTap={{ scale: 0.98 }}
                                onClick={upnavigate}
                                disabled={isloading}
                            >
                                Analyse Resume →
                            </motion.button>
                            <motion.button
                                whileHover={{ y: -2 }}
                                whileTap={{ scale: 0.98 }}
                                onClick={generateMentor}
                                disabled={mentorLoading}
                                className={Styles.secondaryButton}
                            >
                                {mentorLoading ? "Generating..." : "Ask AI Mentor"}
                            </motion.button>
                        </div>
                        <div className={Styles.statusGrid}>
                            <div className={Styles.statusCard}>
                                <span>Profile Completion</span>
                                <strong>{profileCompletion}%</strong>
                            </div>
                            <div className={Styles.statusCard}>
                                <span>Last ATS Ready</span>
                                <strong>{isprevious ? "Available" : "No report"}</strong>
                            </div>
                        </div>
                        {completionMessage && (
                            <div className={Styles.warningCard}>
                                {completionMessage}
                            </div>
                        )}
                    </motion.div>
                </section>

               <section className={Styles.mentorSection}>
    <motion.div
        className={Styles.historyPanel}
        initial={{ opacity: 0, y: 24 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.5, delay: 0.12 }}
    >

        <div className={Styles.historyHeader}>
            <h2>AI Mentor Summary</h2>
            <p>
                Quick overview of your latest AI career guidance and recommendations.
            </p>
        </div>

        {mentorLatest ? (

            <div className={Styles.historyList}>

                <div className={Styles.historyCard}>

                    <div className={Styles.historyMeta}>
                        <span>
                            {new Date(
                                mentorLatest.createdAt
                            ).toLocaleDateString()}
                        </span>

                        <strong>
                            AI Mentor Report
                        </strong>
                    </div>

                    <p>
                        {
                            mentorLatest.summary
                            ?
                            mentorLatest.summary.slice(0, 220) +
                            (
                                mentorLatest.summary.length > 220
                                ? "..."
                                : ""
                            )
                            :
                            "Your AI mentor generated personalized career guidance and improvement suggestions."
                        }
                    </p>

                    <div className={Styles.historyMeta}>

                        <button
                            className={Styles.secondaryButton}
                            onClick={() =>
                                navigate(
                                    `/ai-mentor/${mentorLatest.id}`
                                )
                            }
                        >
                            See Full Details
                        </button>

                    </div>

                </div>

            </div>

        ) : (

            <div className={Styles.emptyState}>

                <p>
                    No AI mentor history available yet.
                    Generate your first AI mentor report.
                </p>

            </div>

        )}

    </motion.div>
</section>

                <section className={Styles.historySection}>
                    <motion.div
                        className={Styles.historyPanel}
                        initial={{ opacity: 0, y: 24 }}
                        animate={{ opacity: 1, y: 0 }}
                        transition={{ duration: 0.5, delay: 0.15 }}
                    >
                        <div className={Styles.historyHeader}>
                            <h2>Previous Resume History</h2>
                            <p>Review your last AI analysis entries and keep your learning plan aligned.</p>
                        </div>
                        {history.length > 0 ? (
                            <div className={Styles.historyList}>
                                {history.map((item) => (
                                    <div key={item.id} className={Styles.historyCard}>
                                        <div className={Styles.historyMeta}>
                                            <span>{new Date(item.createdAt).toLocaleDateString()}</span>
                                            <strong>{item.roles || "Resume analysis"}</strong>
                                        </div>
                                        <div className={Styles.historyBadges}>
                                            <span>Score: {item.score}</span>
                                            <span>ATS: {item.atsoptimizationscore}</span>
                                        </div>
                                        <p>{item.resumeSummary || "Uploaded resume summary not available."}</p>
                                        <div className={Styles.historyMeta}>
                                            <button className={Styles.secondaryButton} onClick={() => navigate(`/analysis/${item.id}`)}>
                                                View Full Details
                                            </button>
                                        </div>
                                    </div>
                                ))}
                            </div>
                        ) : (
                            <div className={Styles.emptyState}>
                                <p>No previous analysis history yet. Upload a resume to get the first AI report.</p>
                            </div>
                        )}
            </motion.div>
                </section>
            </main>

            <AnimatePresence>
                {showLoginPrompt && (
                    <div className={Styles.promptOverlay}>
                        <motion.div
                            className={Styles.promptCard}
                            initial={{ opacity: 0, scale: 0.95 }}
                            animate={{ opacity: 1, scale: 1 }}
                            exit={{ opacity: 0, scale: 0.95 }}
                        >
                            <h3>Get better AI suggestions</h3>
                            <p>Sign in to access personalized career guidance, resume feedback, and mentor-style recommendations.</p>
                            <div className={Styles.promptActions}>
                                <button onClick={() => { navigate('/login'); }}>
                                    Login
                                </button>
                                <button className={Styles.promptClose} onClick={closeLoginPrompt}>
                                    Maybe later
                                </button>
                            </div>
                        </motion.div>
                    </div>
                )}
            </AnimatePresence>

            <AnimatePresence>
                {showDeleteModal && (
                    <div className={Styles.delcontainer}>
                        <motion.div
                            className={Styles.modalBackdrop}
                            initial={{ opacity: 0 }}
                            animate={{ opacity: 1 }}
                            exit={{ opacity: 0 }}
                            onClick={() => !delloading && setShowDeleteModal(false)}
                        />
                        <motion.div
                            className={Styles.confirmcontainer}
                            initial={{ opacity: 0, scale: 0.95, y: 15 }}
                            animate={{ opacity: 1, scale: 1, y: 0 }}
                            exit={{ opacity: 0, scale: 0.95, y: 15 }}
                        >
                            <div className={Styles.modalHeaderContainer}>
                                <span className={Styles.modalIcon}>⚠️</span>
                                <h3>Delete Account?</h3>
                            </div>
                            <p>
                                Are you sure you want to delete your account? It will <strong>permanently remove all your data</strong> from our secure servers. This change cannot be reverted.
                            </p>
                            <div className={Styles.confirmationbtns}>
                                <button className={Styles.notnow} disabled={delloading} onClick={() => setShowDeleteModal(false)}>
                                    Not now
                                </button>
                                <button className={Styles.confirmdel} disabled={delloading} onClick={delaccount}>
                                    {delloading ? "Deleting..." : "Delete Profile"}
                                </button>
                            </div>
                        </motion.div>
                    </div>
                )}
            </AnimatePresence>
        </div>
    );
}

export default Home;
