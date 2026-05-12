import { BrowserRouter, Routes, Route} from "react-router";
// import { ScrollToTop } from "./components/UI/ScrollToTop";

import Home from "./pages/HomePage";
import Header from "./components/Header";
import MarketsOverview from "./pages/MarketsOverviewPage";



function App() {
  return (
    <BrowserRouter>
    {/* <ScrollToTop/> */}
      <div className="pb-10">
        <Header />
      </div>
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 min-h-screen pt-16">
        <Routes>
          <Route path="/" element={<Home/>}/>
          <Route path="/markets" element={<MarketsOverview/>}/>
        </Routes>
      </main>
    </BrowserRouter>
  );
}

export default App;